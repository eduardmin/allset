package com.allset.allset.config

import com.allset.allset.model.User
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Component

/**
 * Ensures a unique index on users.email at startup. Mongo auto-index-creation is
 * disabled, so annotations alone do not create indexes.
 *
 * Guarded on purpose: if duplicate emails still exist in the collection the index
 * build fails, and we log a clear warning instead of crashing the app. Remove the
 * duplicates and restart to activate the constraint. Once active, it also makes the
 * race-safe get-or-create in AuthenticationService / UserService effective by
 * turning a concurrent double-insert into a DuplicateKeyException.
 */
@Component
class UserIndexInitializer(
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(UserIndexInitializer::class.java)

    @PostConstruct
    fun ensureUserIndexes() {
        runCatching {
            mongoTemplate.indexOps(User::class.java).ensureIndex(
                Index().on("email", Sort.Direction.ASC).unique().named("uniq_email")
            )
            logger.info("Ensured unique index on users.email")
        }.onFailure { ex ->
            logger.error(
                "Failed to create unique index on users.email — this usually means " +
                    "duplicate email documents already exist. Remove the duplicates and " +
                    "restart to enforce email uniqueness.",
                ex
            )
        }
    }
}
