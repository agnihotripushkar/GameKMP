package com.devpush.features.userRatingsReviews.ui.components

/**
 * This file serves as a central export point for all user rating and review UI components.
 * 
 * Available components:
 * 
 * ## StarRating
 * - `StarRating`: Interactive star rating component (1-5 stars) with read-only and editable modes
 * - `CompactStarRating`: Compact version for small spaces like game cards
 * 
 * ## ReviewCard
 * - `ReviewCard`: Card component for displaying reviews with edit/delete options
 * - `CompactReviewCard`: Compact version with limited lines
 * 
 * ## ReviewDialog
 * - `ReviewDialog`: Dialog for writing and editing reviews with character counter and validation
 * 
 * ## QuickRating
 * - `QuickRating`: Quick rating component for collection views with long-press interaction
 * - `QuickRatingDialog`: Dialog for quick rating selection (internal component)
 * 
 * ## Usage Examples:
 * 
 * ```kotlin
 * // Interactive star rating
 * StarRating(
 *     rating = userRating,
 *     onRatingChanged = { newRating -> viewModel.setRating(newRating) }
 * )
 * 
 * // Read-only compact rating for game cards
 * CompactStarRating(rating = userRating)
 * 
 * // Review display with actions
 * ReviewCard(
 *     review = userReview,
 *     onEditClick = { showEditDialog = true },
 *     onDeleteClick = { viewModel.deleteReview() }
 * )
 * 
 * // Review writing/editing dialog
 * ReviewDialog(
 *     isVisible = showReviewDialog,
 *     initialReviewText = existingReview?.reviewText ?: "",
 *     onSave = { reviewText -> viewModel.saveReview(reviewText) },
 *     onDismiss = { showReviewDialog = false },
 *     isEditing = existingReview != null
 * )
 * 
 * // Quick rating for collection views
 * QuickRating(
 *     currentRating = userRating,
 *     onRatingChanged = { newRating -> viewModel.setRating(newRating) },
 *     showRatingDialog = showQuickRatingDialog,
 *     onShowRatingDialog = { showQuickRatingDialog = true },
 *     onHideRatingDialog = { showQuickRatingDialog = false }
 * )
 * ```
 */