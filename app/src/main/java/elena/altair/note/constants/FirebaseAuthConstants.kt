package elena.altair.note.constants

object FirebaseAuthConstants {
    const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
    const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
    const val ERROR_INVALID_CREDENTIAL =
        "ERROR_INVALID_CREDENTIAL" // неверный пароль или почта, или и то и другое
    const val ERROR_WRONG_PASSWORD =
        "ERROR_WRONG_PASSWORD" // неверный пароль или почта, или и то и другое (для старых систем)
    const val ERROR_WEAK_PASSWORD =
        "ERROR_WEAK_PASSWORD" // Password should be at least 6 characters
}