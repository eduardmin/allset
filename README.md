# allset

## Pricing and Promo Codes

- Configure the standard invitation price in `src/main/resources/application.yml` under `app.pricing.base-price`.
- Define promo codes in the same file within the `app.promo-codes.codes` list. Each entry accepts the promo `code`, a `discount-type` (`PERCENTAGE` or `AMOUNT`), the numeric `discount-value`, and optional `starts-at` / `expires-at` timestamps. Codes are normalized to uppercase during synchronization so matching is case-insensitive for users.
- Promo codes from the configuration are synchronized into MongoDB on startup so they can be applied by users from their profile.
