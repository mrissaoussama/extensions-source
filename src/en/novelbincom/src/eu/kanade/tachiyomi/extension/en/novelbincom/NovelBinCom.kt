package eu.kanade.tachiyomi.novelextension.en.novelbincom

import eu.kanade.tachiyomi.multisrc.readnovelfull.ReadNovelFull

class NovelBinCom :
    ReadNovelFull(
        name = "Novel-Bin (com)",
        baseUrl = "https://novel-bin.com",
        lang = "en",
    ) {
    override val popularPage = "monthvisit"
    override val latestPage = "dayvisit"

    // Chapters are listed in full on the novel page (ul.list-chapter); there is no ajax endpoint.
    override val noAjax = true
}
