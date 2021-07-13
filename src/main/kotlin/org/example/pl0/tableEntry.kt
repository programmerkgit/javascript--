package org.example.pl0

enum class VarKind {
    ParId,
    VarId,
    ConstId,
    FuncId,
}

abstract class TableEntry(
    var kind: VarKind,
    var name: String
)

class FuncEntry(
    name: String,
    val level: Int,
    var rAddr: Int,
    var parCount: Int
) : TableEntry(VarKind.FuncId, name)

/* How Par Entry Is used */
class ParEntry(
    name: String,
    var level: Int,
    var parAddr: Int = 0
) : TableEntry(VarKind.ParId, name)

class VarEntry(
    name: String,
    var level: Int,
    var addr: Int
) : TableEntry(VarKind.VarId, name)

class ConstEntry(
    name: String,
    var value: Int
) : TableEntry(VarKind.ConstId, name)
