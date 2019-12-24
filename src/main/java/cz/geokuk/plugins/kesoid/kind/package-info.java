/**
 * Nadřazený balík pro různé druhy kešoidů.
 * V tomto balíku jsou rozhraní a obecný kód pro výběr implemetnací různých druhů kešoidů.
 *
 * Tento balík vidí všechy podřízené balíky, ale jan jako seznam implemetancí pluginů, protože nemáme žádný pluginovací a scanovací framework.
 * Jinak nesmí vidět nic.
 * Jednotlivé implemetnace vidí tento balíka a mohouvidět i nahoru.
 * Zbytedk košoidů vidí tento balík, ale nesmí vidět žádné podřízené balíky.
 */
package cz.geokuk.plugins.kesoid.kind;