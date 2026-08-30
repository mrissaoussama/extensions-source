package eu.kanade.tachiyomi.novelextension.en.novelbincom

import eu.kanade.tachiyomi.multisrc.readnovelfull.ReadNovelFull
import keiyoushi.annotation.Source

@Source
abstract class NovelBinCom : ReadNovelFull() {
    override val popularPage = "monthvisit"
    override val latestPage = "dayvisit"
    override val noAjax = true
}
