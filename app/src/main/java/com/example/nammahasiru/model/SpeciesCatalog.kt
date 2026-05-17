package com.example.nammahasiru.model

object SpeciesCatalog {
    val SPECIES_NAMES: List<String> = listOf(
        "Neem",
        "Peepal",
        "Banyan",
        "Mango",
        "Tamarind",
        "Coconut",
        "Jackfruit",
        "Ashoka",
        "Gulmohar",
        "Rain Tree",
    )

    /**
     * Illustrative survival rates used only for ordering the fallback guide when local data is sparse.
     */
    val FALLBACK_SURVIVAL_RATES: Map<String, Float> = mapOf(
        "Neem" to 0.82f,
        "Peepal" to 0.80f,
        "Banyan" to 0.79f,
        "Mango" to 0.74f,
        "Tamarind" to 0.72f,
        "Coconut" to 0.70f,
        "Jackfruit" to 0.68f,
        "Ashoka" to 0.65f,
        "Gulmohar" to 0.58f,
        "Rain Tree" to 0.55f,
    )
}
