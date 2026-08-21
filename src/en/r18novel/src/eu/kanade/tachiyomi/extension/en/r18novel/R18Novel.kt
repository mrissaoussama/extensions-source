package eu.kanade.tachiyomi.extension.en.r18novel

import eu.kanade.tachiyomi.multisrc.readnovelfull.ReadNovelFull
import keiyoushi.annotation.Source
import keiyoushi.utils.SlugPath

/**
 * r18novel.com runs the same site engine as the other ReadNovelFull-family sites (identical
 * `novel-title`/`info-meta`/`desc-text`/`chr-content` markup), just with a `/webnovel/<id>/`
 * detail-url shape instead of the family's usual `/<slug>.html`, and its own popular/latest paths.
 */
@Source
abstract class R18Novel : ReadNovelFull() {
    override val mangaPathTemplate = SlugPath("/webnovel/", "/")

    override val popularPage = "allvisit"
    override val latestPage = "dayvisit"

    // Chapters are already inline on the novel page itself (confirmed live); skip the
    // ajax/chapter-archive probe the base theme otherwise tries first.
    override val noAjax = true
}
