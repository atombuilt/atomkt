package com.atombuilt.atomkt.commons.regex

public object CommonRegex {

    public val EMAIL: Regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)
    public val URL: Regex =
        "(?<protocol>\\w*)\\:\\/\\/(?:(?:(?<thld>[\\w\\-]*)(?:\\.))?(?<sld>[\\w\\-]*))\\.(?<tld>\\w*)(?:\\:(?<port>\\d*))?".toRegex()
}
