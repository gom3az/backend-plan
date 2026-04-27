package com.gomaa.tasks.exceptions

class ApiValidationException(errors: List<String>) : RuntimeException(errors.joinToString("; "))
