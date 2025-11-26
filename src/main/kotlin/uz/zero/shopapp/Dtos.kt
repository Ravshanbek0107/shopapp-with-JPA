package uz.zero.shopapp

import java.math.BigDecimal
import java.util.Date

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
    val count: Long,
    val category: CategoryResponse
) {
    companion object {
        fun toResponse(product: Product) = ProductResponse(
            id = product.id!!,
            name = product.name.localized(),
            count = product.count,
            category = CategoryResponse.toAdminResponse(product.category)
        )
    }
}

data class UserRequest(
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
    val amount: BigDecimal,
    val date: Date
) {
    companion object {
        fun toResponse(payment: UserPaymentTransaction) = UserPaymentResponse(
            id = payment.id!!,
            userId = payment.user.id!!,
            amount = payment.amount,
            date = payment.date
        )
    }
}

data class BuyItemRequest(
    val productId: Long,
    val count: Long,
    val amount: BigDecimal
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
    val items: List<BuyItemResponse>
) {
    companion object {
        fun toResponse(transaction: Transaction, items: List<TransactionItem>) = BuyResponse(
            transactionId = transaction.id!!,
            userId = transaction.user.id!!,
            totalAmount = transaction.totalAmount,
            date = transaction.date,
            items = items.map { BuyItemResponse.toResponse(it) }
        )
    }
}

data class BuyItemResponse(
    val productId: Long,
    val productName: String,
    val count: Long,
    val amount: BigDecimal,
    val totalAmount: BigDecimal
) {
    companion object {
        fun toResponse(item: TransactionItem) = BuyItemResponse(
            productId = item.product.id!!,
            productName = item.product.name.localized(),
            count = item.count,
            amount = item.amount,
            totalAmount = item.totalAmount
        )
    }
}

data class TransactionResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val totalAmount: BigDecimal,
    val date: Date
) {
    companion object {
        fun toResponse(transaction: Transaction) = TransactionResponse(
            id = transaction.id!!,
            userId = transaction.user.id!!,
            userName = transaction.user.fullname,
            totalAmount = transaction.totalAmount,
            date = transaction.date
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

data class CheckBalanceRequest(
    val requiredAmount: BigDecimal
)

data class CheckBalanceResponse(
    val hasEnoughBalance: Boolean,
    val currentBalance: BigDecimal,
    val requiredAmount: BigDecimal
) {
    companion object {
        fun toResponse(hasEnough: Boolean, current: BigDecimal, required: BigDecimal) = CheckBalanceResponse(
            hasEnoughBalance = hasEnough,
            currentBalance = current,
            requiredAmount = required
        )
    }
}