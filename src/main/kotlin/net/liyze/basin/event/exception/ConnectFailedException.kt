package net.liyze.basin.event.exception

class ConnectFailedException(url: String) : RuntimeException("URL: $url")