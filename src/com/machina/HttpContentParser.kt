package com.machina

import java.util.regex.Pattern

object HttpContentParser {

    fun parseBody(httpContent: String): HttpContent {
        println("Parsing text/html content. . .\n\n")
        val content = removeScript(httpContent)
        var processedContent = removeEntities(removeComment(removeLink(content)))

        var title = ""

        var regex = "(?<=<title>)[\\s\\S](.*)(?=</title>)".toRegex()
        var matchedString : MatchResult? = regex.find(processedContent, 0)
        processedContent = regex.replace(processedContent, "")
        if (matchedString != null) {
            title = matchedString.value
        }

        regex = "<body[^>]*>[\\s\\S]*?</body>".toRegex()
        matchedString = regex.find(processedContent, 0)
        if (matchedString != null) {
            processedContent = matchedString.value
        }

        processedContent = removeAllTags(processedContent)
        processedContent = removeSpaces(processedContent)
        processedContent = removeNoContentLink(processedContent)
        processedContent = changeATagtoNewline(processedContent)

        val clickableLinks: MutableList<String> = ArrayList()

        val patternTag = Pattern.compile("(?i)<a([^>]+)>(.*)")
        val patternLink = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))")

        val matcherTag = patternTag.matcher(processedContent)

        while (matcherTag.find()) {
            val href = matcherTag.group(1)
            val linkText = matcherTag.group(2)

            val matcherLink = patternLink.matcher(href)
            if (matcherLink.find()) {
                var link = matcherLink.group(1)
                val hashRegex = "\"#.*\"".toRegex()
                link = hashRegex.replace(link, "")
                if(link.equals("")) {
                    clickableLinks.add("$linkText")
                }
                else {
                    clickableLinks.add("$linkText -> $link")
                }

            } else {
                clickableLinks.add(linkText)
            }

        }

        for (link in clickableLinks) {
            regex = "<a[^>]*>[\\s\\S].*".toRegex()
            processedContent = regex.replaceFirst(processedContent, link)
        }

//        println(title)
//        println(processedContent)

        return HttpContent(
            title = title,
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

    private fun removeNoContentLink(content: String): String {
        val regex = "<a[^>]*></a>".toRegex()
        return regex.replace(content, "")
    }

    private fun changeATagtoNewline(content: String): String {
        val regex = "</a>".toRegex()
        return regex.replace(content, "\n")
    }

    private fun removeEntities(content: String): String {
        var temp = content
        temp = temp.replace("&nbsp;", " ")
        temp = temp.replace("&lt;", "<")
        temp = temp.replace("&gt;", ">")
        temp = temp.replace("&amp;", "&")
        temp = temp.replace("&quot;", "\"\"")
        temp = temp.replace("&apos;", "\'\'")

        temp = temp.replace("&nbsp", " ")
        temp = temp.replace("&lt", "<")
        temp = temp.replace("&gt", ">")
        temp = temp.replace("&amp", "&")
        temp = temp.replace("&quot", "\"\"")
        temp = temp.replace("&apos", "\'\'")

        return temp
    }
}