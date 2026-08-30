package eu.bcosp.vrlintellij.inspections

/** Shared "did you mean" helper for name-lookup inspections (unknown function, unresolved variable). */
internal fun closestMatch(name: String, candidates: Collection<String>): String? {
    val maxDistance = if (name.length <= 4) 1 else 2
    return candidates
        .map { it to levenshteinDistance(name, it) }
        .filter { it.second <= maxDistance }
        .minByOrNull { it.second }
        ?.first
}

private fun levenshteinDistance(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            dp[i][j] = if (a[i - 1] == b[j - 1]) {
                dp[i - 1][j - 1]
            } else {
                1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
    }
    return dp[a.length][b.length]
}
