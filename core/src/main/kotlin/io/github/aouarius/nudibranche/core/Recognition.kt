package io.github.aouarius.nudibranche.core

/** A species the recognition model thinks is on the photo, with its probability (0–1). */
data class Suggestion(val species: Species, val confidence: Float)

object Recognition {
    /** Below this the best guess is shown as uncertain. */
    const val CONFIDENT = 0.5f

    /** Below this a species is not offered at all, so a photo of an unknown animal gets no suggestions. */
    const val MINIMUM = 0.08f

    /**
     * Turns the model output into at most [count] suggestions, best first. [labels] are the
     * species ids in the model's output order; ids missing from [catalog] are skipped.
     */
    fun suggestions(
        probabilities: FloatArray,
        labels: List<String>,
        catalog: List<Species>,
        count: Int = 3,
    ): List<Suggestion> {
        require(probabilities.size == labels.size) { "Model has ${probabilities.size} outputs for ${labels.size} labels" }
        val byId = catalog.associateBy { it.id }
        return probabilities.indices
            .sortedByDescending { probabilities[it] }
            .filter { probabilities[it] >= MINIMUM }
            .mapNotNull { index -> byId[labels[index]]?.let { Suggestion(it, probabilities[index]) } }
            .take(count)
    }
}
