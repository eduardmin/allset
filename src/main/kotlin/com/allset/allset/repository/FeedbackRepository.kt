package com.allset.allset.repository

import com.allset.allset.model.FeedbackItem
import org.springframework.data.mongodb.repository.MongoRepository

interface FeedbackRepository : MongoRepository<FeedbackItem, String>
