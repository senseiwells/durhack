package com.durhack.tmp.actions

import com.durhack.tmp.Ant

sealed interface Action {
    val ant: Ant

    fun act()
}