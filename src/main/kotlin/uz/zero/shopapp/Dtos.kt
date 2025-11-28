package uz.zero.shopapp

import java.math.BigDecimal
import java.util.Date

data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}

data class LocalizedNameRequest(
    val uz: String,
    val ru: String,
    val en: String
)

data class LocalizedNameResponse(
    val uz: String,
    val ru: String,
    val en: String
)

data class CategoryCreateRequest(
    val name: String,
    val description: String,
    val order: Long,
)

data class CategoryUpdateRequest(
    val name: String?,
    val order: Long?,
)

data class CategoryResponse(
    var id: Long,
    val name: String,
    val order: Long,
){
    companion object {
        fun toResponse(category: Category) = run {
            CategoryResponse(
                category.id!!,
                category.name,
                category.order
            )
        }
    }
}

data class ProductCreateRequest(
    val name: LocalizedNameRequest,
    val count: Long,
    val amount : BigDecimal,
    val categoryId: Long
)

data class ProductUpdateRequest(
    val name: LocalizedNameRequest?,
    val count: Long?,
    val amount: BigDecimal?,
    val categoryId: Long?
)

data class ProductResponse(
    var id: Long,
    val name: String,
    val count: Long,
    val amount: BigDecimal,
    val category: CategoryResponse
) {
    companion object {
        fun toResponse(product: Product) = ProductResponse(
            id = product.id!!,
            name = product.name.localized(),
            count = product.count,
            amount = product.amount,
            category = CategoryResponse.toResponse(product.category)
        )
    }
}

data class UserCreateRequest(
    val fullname: String,
    val username: String
)

data class UserResponse(
    val id: Long,
    val fullname: String,
    val username: String,
    val balance: BigDecimal
) {
    companion object {
        fun toResponse(user: User) = UserResponse(
            id = user.id!!,
            fullname = user.fullname,
            username = user.username,
            balance = user.balance
        )
    }
}

data class UserPaymentRequest(
    val userId: Long,
    val amount: BigDecimal
)

data class UserPaymentResponse(
    val id: Long,
    val userId: Long,
    val newBalance: BigDecimal,
    val date: Date
) {
    companion object {
        fun toResponse(payment: UserPaymentTransaction) = UserPaymentResponse(
            id = payment.id!!,
            userId = payment.user.id!!,
            newBalance = payment.amount,
            date = payment.date
        )
    }
}

data class UserPaymentHistoryResponse(
    val id: Long,
    val userId: Long,
    val amount: BigDecimal,
    val date: Date
){
    companion object {
        fun toResponse(history: UserPaymentTransaction) = UserPaymentHistoryResponse(
            id = history.id!!,
            userId = history.user.id!!,
            amount = history.amount,
            date = history.date
        )
    }
}

data class UserBuyHistoryResponse(
    val transactionId: Long,
    val totalAmount: BigDecimal,
    val date: Date,
    val items: List<TransactionItemResponse>
) {
    companion object {
        fun toResponse(transaction: Transaction, items: List<TransactionItem>) = UserBuyHistoryResponse(
            transactionId = transaction.id!!,
            totalAmount = transaction.totalAmount,
            date = transaction.date,
            items = items.map { TransactionItemResponse.toResponse(it) }
        )
    }
}


data class BuyItemRequest(
    val productId: Long,
    val count: Long,
)


data class BuyRequest(
    val userId: Long,
    val items: List<BuyItemRequest>
)

data class BuyResponse(
    val transactionId: Long,
    val userId: Long,
    val totalAmount: BigDecimal,
    val date: Date,
    val items: List<TransactionItemResponse>
) {
    companion object {
        fun toResponse(transaction: Transaction, items: List<TransactionItem>) = BuyResponse(
            transactionId = transaction.id!!,
            userId = transaction.user.id!!,
            totalAmount = transaction.totalAmount,
            date = transaction.date,
            items = items.map { TransactionItemResponse.toResponse(it) }
        )
    }
}

data class TransactionItemResponse(
    val id: Long,
    val productName: String,
    val count: Long,
    val amount: BigDecimal,
    val totalAmount: BigDecimal
) {
    companion object {
        fun toResponse(item: TransactionItem) = TransactionItemResponse(
            id = item.id!!,
            productName = item.product.name.localized(),
            count = item.count,
            amount = item.amount,
            totalAmount = item.totalAmount
        )
    }
}

data class AllTransactionsResponse(
    val transactionId: Long,
    val user: UserResponse,
    val totalAmount: BigDecimal,
    val date: Date
)
