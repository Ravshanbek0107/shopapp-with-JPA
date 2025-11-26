package uz.zero.shopapp



data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}

data class CategoryRequest(
    val name: String,
    val description: String,
    val order: Long,
)

data class CategoryResponse(
    var id: Long,
    val name: String,
    val description: String,
    val order: Long,
){
    companion object {
        fun toAdminResponse(category: Category) = run {
            CategoryResponse(category.id!!,
                category.name,
                category.description,
                category.order)
        }

    }
}

data class ProductResponse(
    var id: Long,
    val name: String,
)