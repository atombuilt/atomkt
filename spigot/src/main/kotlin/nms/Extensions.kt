package com.atombuilt.atomkt.spigot.nms

import org.bukkit.Server

/**
 * Returns the server version from NMS package.
 */
public val NMSVersion: String = Server::class.java.getPackage().name.replace(".", ",").split(",")[3]
