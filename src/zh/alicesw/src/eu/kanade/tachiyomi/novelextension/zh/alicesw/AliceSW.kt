package eu.kanade.tachiyomi.novelextension.zh.alicesw

import eu.kanade.tachiyomi.source.NovelSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.annotation.Source
import keiyoushi.network.get
import keiyoushi.source.KeiSource
import keiyoushi.utils.SlugPath
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.nodes.Document

@Source
abstract class AliceSW :
    KeiSource(),
    NovelSource {

    private val mangaPath = SlugPath("/novel/", ".html")
    private val chapterPath = SlugPath("/book/", ".html")

    override fun getMangaUrl(manga: SManga): String = mangaPath.absolute(baseUrl, manga.url)

    override fun getChapterUrl(chapter: SChapter): String = chapterPath.absolute(baseUrl, chapter.url)

    override suspend fun getPopularManga(page: Int): MangasPage = parseMangaList(client.get("$baseUrl/all/order/hits+desc.html?page=$page", headers))

    override suspend fun getLatestUpdates(page: Int): MangasPage = parseMangaList(client.get("$baseUrl/all/order/update_time+desc.html?page=$page", headers))

    override suspend fun getSearchMangaList(page: Int, query: String, filters: FilterList): MangasPage {
        if (query.isBlank()) return getPopularManga(page)
        val url = "$baseUrl/search.html".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("f", "title")
            .addQueryParameter("page", page.toString())
            .build()
        return parseMangaList(client.get(url, headers))
    }

    private fun parseMangaList(response: Response): MangasPage {
        val document = response.asJsoup()
        val seen = mutableSetOf<String>()

        val mangas = document.select("a[href~=^/novel/\\d+\\.html$]").mapNotNull { element ->
            val href = element.attr("href")
            val title = element.text()
            if (title.isBlank() || !seen.add(href)) return@mapNotNull null

            SManga.create().apply {
                setSlugUrl(mangaPath, href)
                this.title = title
            }
        }

        val hasNextPage = document.selectFirst("a:contains(下一页)") != null
        return MangasPage(mangas, hasNextPage)
    }

    override suspend fun getMangaByUrl(url: HttpUrl): SManga? {
        val path = url.encodedPath
        if (!path.startsWith("/novel/")) return null
        val manga = SManga.create().apply { setSlugUrl(mangaPath, path) }
        return fetchMangaUpdate(manga, emptyList(), fetchDetails = true, fetchChapters = false).manga
    }

    override suspend fun fetchMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate {
        val updatedManga = if (fetchDetails) {
            val response = client.get(mangaPath.absolute(baseUrl, manga.url), headers)
            parseMangaDetails(response.asJsoup(), manga)
        } else {
            manga
        }
        val updatedChapters = if (fetchChapters) loadChapterList(manga) else chapters

        return SMangaUpdate(updatedManga, updatedChapters)
    }

    private fun parseMangaDetails(document: Document, manga: SManga): SManga {
        val description = document.selectFirst(".jianjie p")?.text()

        val category = document.select("a[href*=\"/lists/\"]")
            .map { it.text() }
            .firstOrNull { it.isNotBlank() && it != "首页" }
        val tags = document.select("a[href*=\"f=tag\"]").map { it.text() }.filter { it.isNotBlank() }

        val pageText = document.text()
        val status = when {
            pageText.contains("已完结") || pageText.contains("完本") -> SManga.COMPLETED
            pageText.contains("连载中") -> SManga.ONGOING
            else -> SManga.UNKNOWN
        }

        val img = document.selectFirst(".pic img")

        return SManga.create().apply {
            url = manga.url
            title = document.selectFirst(".novel_title")?.text() ?: manga.title
            author = document.selectFirst("a[href*=\"f=author\"]")?.text()
            genre = (listOfNotNull(category) + tags).joinToString(", ")
            this.status = status
            this.description = description
            thumbnail_url = img?.attr("data-src")?.ifBlank { img.attr("src") }
        }
    }

    private suspend fun loadChapterList(manga: SManga): List<SChapter> {
        val novelId = manga.url
        val response = client.get("$baseUrl/other/chapters/id/$novelId.html", headers)
        val document = response.asJsoup()

        return document.select("ul.mulu_list li a").mapIndexedNotNull { index, element ->
            val href = element.attr("href")
            val name = element.text()
            if (href.isBlank() || name.isBlank()) return@mapIndexedNotNull null

            SChapter.create().apply {
                setSlugUrl(chapterPath, href)
                this.name = name
                chapter_number = (index + 1).toFloat()
            }
        }
    }

    override suspend fun getPageList(chapter: SChapter): List<Page> = listOf(Page(0, chapterPath.absolute(baseUrl, chapter.url)))

    override suspend fun fetchPageText(page: Page): String {
        val document = client.get(page.url, headers).asJsoup()
        val content = document.selectFirst(".j_readContent") ?: document.selectFirst(".read-content")
            ?: throw Exception("Chapter content not found")
        return content.html()
    }
}
