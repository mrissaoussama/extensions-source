package eu.kanade.tachiyomi.novelextension.en.novelbincom

import eu.kanade.tachiyomi.multisrc.readnovelfull.ReadNovelFull

class NovelBinCom : ReadNovelFull() {
    override val name = "Novel-Bin (com)"
    override val baseUrl = "https://novel-bin.com"
    override val lang = "en"

    override val popularPage = "monthvisit"
    override val latestPage = "dayvisit"

    // Chapters are listed in full on the novel page (ul.list-chapter); there is no ajax endpoint.
    override val noAjax = true
}
