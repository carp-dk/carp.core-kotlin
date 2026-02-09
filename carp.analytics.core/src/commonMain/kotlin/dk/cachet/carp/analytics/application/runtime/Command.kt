package dk.cachet.carp.analytics.application.runtime

data class Command(
    val exe: String,
    val args: List<String> = emptyList(),
    val cwd: String?,
    val env: Map<String, String> = emptyMap(),
    val stdin: ByteArray?,
    val timeoutMs: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Command

        if (timeoutMs != other.timeoutMs) return false
        if (exe != other.exe) return false
        if (args != other.args) return false
        if (cwd != other.cwd) return false
        if (env != other.env) return false
        if (stdin != null) {
            if (other.stdin == null) return false
            if (!stdin.contentEquals(other.stdin)) return false
        } else if (other.stdin != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeoutMs?.hashCode() ?: 0
        result = 31 * result + exe.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + (cwd?.hashCode() ?: 0)
        result = 31 * result + env.hashCode()
        result = 31 * result + (stdin?.contentHashCode() ?: 0)
        return result
    }
}
