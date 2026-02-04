package com.allset.allset.model

enum class ConfirmationFilter(val id: String) {
    SHOW_ALL_GUESTS("show_all_guests"),
    SHOW_ONLY_ADDED_BY_ME("show_only_added_by_me"),
    SHOW_DELETED_HIDDEN("show_deleted_hidden"),
    SHOW_ONLY_CONFIRMED("show_only_confirmed"),
    SHOW_ONLY_NOT_COMING("show_only_not_coming"),
    SHOW_ONLY_GROOM_GUESTS("show_only_groom_guests"),
    SHOW_ONLY_BRIDE_GUESTS("show_only_bride_guests"),
    SHOW_GUESTS_WITHOUT_TABLE("show_guests_without_table");

    companion object {
        fun fromId(id: String): ConfirmationFilter? {
            return values().find { it.id == id }
        }
    }
}


