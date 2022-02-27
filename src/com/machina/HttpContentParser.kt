package com.machina

object HttpContentParser {
    fun parseBody(httpResult: HttpResult): HttpContent {
        val content = httpResult.content.toString()
        var processedContent = removeComment(removeLink(removeScript(content)))


        var title = ""

        var regex = "(?<=<title>)[\\s\\S](.*)(?=</title>)".toRegex()
        var matchedString : MatchResult? = regex.find(processedContent, 0)
        processedContent = regex.replace(processedContent, "")
        if (matchedString != null) {
            title = matchedString.value
        }

        val rawATags: MutableList<String> = ArrayList()

        regex = "<a[^>]*>[\\s\\S]*?</a>".toRegex()
        var sequenceMatchedString : Sequence<MatchResult> = regex.findAll(processedContent, 0)
        if (sequenceMatchedString != null) {
            sequenceMatchedString.forEach()
            {
                    matchResult -> rawATags.add(matchResult.value)
            }
        }

        regex = "<body[^>]*>[\\s\\S]*?</body>".toRegex()
        matchedString = regex.find(processedContent, 0)
        if (matchedString != null) {
            processedContent = matchedString.value
        }

        processedContent = removeAllTags(processedContent)
        processedContent = removeSpaces(processedContent)

        val clickableLinks: MutableList<String> = ArrayList()
        clickableLinks.add("")
        println(title)
        println(processedContent)
        return HttpContent(
            text = processedContent,
            links = clickableLinks
        )
    }

    private fun removeScript(content: String): String {
        val regex = "<script[^>]*>[\\s\\S]*?</script>".toRegex()
        return regex.replace(content, "")
    }

    private fun removeLink(content: String): String {
        val regex = "<link[^>][\\s\\S]*?/>".toRegex()
        return regex.replace(content, "")
    }

    private fun removeAllTags(content: String): String {
        val regex = "<(?!/?a(?=>|\\s.*>))/?.*?>".toRegex()
        return regex.replace(content, "")
    }

    private fun removeComment(content: String): String {
        val regex = "<!--[\\s\\S]*?-->".toRegex()
        return  regex.replace(content, "")
    }

    private fun removeSpaces(content: String): String {
        val regex = "^\\s+".toRegex(RegexOption.MULTILINE)
        return regex.replace(content, "")
    }

//    private fun splitLinks(content: String): String {
//        // Kode parsing disini
//        val regex = "(?<=<a[^>]*>)[\\s\\S](.*)(?=</a>)".toRegex()
//        val matchedString : Sequence<MatchResult> = regex.findAll(content, 0)
//        if (matchedString != null) {
//            matchedString.forEach()
//            {
//                    matchResult -> println(matchResult.value)
//            }
//        }
//        var processedContent = content
//        return processedContent
//    }
}