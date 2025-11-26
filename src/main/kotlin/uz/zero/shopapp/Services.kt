package uz.zero.shopapp

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


interface UserService {
    fun createUser(fullname: String, username: String): UserResponse
    fun getUserById(id: Long): UserResponse
    fun getUserByUsername(username: String): UserResponse
    fun getUserBalance(username: String): BigDecimal
}

@Service
open class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun createUser(fullname: String, username: String): UserResponse {
        if (userRepository.existsByUsername(username)) {
            throw UserAlreadyExistsException()
        }

        val user = User(
            fullname = fullname,
            username = username,
            balance = BigDecimal.ZERO
        )

        val savedUser = userRepository.save(user)
        return UserResponse.toResponse(savedUser)
    }

    override fun getUserById(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        return UserResponse.toResponse(user)
    }

    override fun getUserByUsername(username: String): UserResponse {
        val user = userRepository.findByUsername(username).orElseThrow { UserNotFoundException() }
        return UserResponse.toResponse(user)
    }

    override fun getUserBalance(username: String): BigDecimal {
        val user = getUserByUsername(username)
        return user.balance
    }

}

interface UserPaymentService {
    fun addBalance(userPaymentRequest: UserPaymentRequest): UserPaymentResponse
    fun getUserPaymentHistory(userId: Long): List<UserPaymentResponse>
}

@Service
class UserPaymentServiceImpl(
    private val userRepository: UserRepository,
    private val userPaymentTransactionRepository: UserPaymentTransactionRepository
) : UserPaymentService {

    @Transactional
    override fun addBalance(userPaymentRequest: UserPaymentRequest): UserPaymentResponse {
        val user = userRepository.findById(userPaymentRequest.userId)
            .orElseThrow { UserNotFoundException() }

        if (userPaymentRequest.amount <= BigDecimal.ZERO) {
            throw InvalidAmountException()
        }

        user.balance = user.balance.add(userPaymentRequest.amount)
        userRepository.save(user)

        val paymentTransaction = UserPaymentTransaction(
            user = user,
            amount = userPaymentRequest.amount
        )

        val savedPayment = userPaymentTransactionRepository.save(paymentTransaction)
        return UserPaymentResponse.toResponse(savedPayment)
    }

    override fun getUserPaymentHistory(userId: Long): List<UserPaymentResponse> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException()
        }

        return userPaymentTransactionRepository.findByUserId(userId).map {
            UserPaymentResponse.toResponse(it)
        }
    }
}


interface TransactionService {
    fun processBuy(buyRequest: BuyRequest): BuyResponse
    fun getUserBuyHistory(userId: Long): List<BuyItemResponse>
    fun getTransactionItems(transactionId: Long): List<TransactionItemResponse>
    fun getAllTransactions(): List<TransactionResponse>
    fun getTransactionProducts(transactionId: Long): List<TransactionItemResponse>
}

@Service
class TransactionServiceImpl(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionItemRepository: TransactionItemRepository
) : TransactionService {

    @Transactional
    override fun processBuy(buyRequest: BuyRequest): BuyResponse {
        val user = userRepository.findById(buyRequest.userId).orElseThrow { UserNotFoundException() }

        var totalAmount = BigDecimal.ZERO

        for (item in buyRequest.items) {
            if (item.count <= 0) {
                throw InvalidAmountException()
            }
            if (item.amount <= BigDecimal.ZERO) {
                throw InvalidAmountException()
            }

            val product = productRepository.findByIdAndDeletedFalse(item.productId)
                ?: throw ProductNotFoundException()

            if (product.count < item.count) {
                throw InsufficientProductException()
            }

            val itemTotal = item.amount.multiply(BigDecimal(item.count))
            totalAmount = totalAmount.add(itemTotal)
        }

        if (user.balance < totalAmount) {
            throw InsufficientBalanceException()
        }

        val transaction = Transaction(
            user = user,
            totalAmount = totalAmount
        )
        val savedTransaction = transactionRepository.save(transaction)

        val transactionItems = mutableListOf<TransactionItem>()

        for (item in buyRequest.items) {
            val product = productRepository.findByIdAndDeletedFalse(item.productId)!!

            product.count -= item.count
            productRepository.save(product)

            val itemTotal = item.amount.multiply(BigDecimal(item.count))

            val transactionItem = TransactionItem(
                product = product,
                count = item.count,
                amount = item.amount,
                totalAmount = itemTotal,
                transaction = savedTransaction
            )
            transactionItems.add(transactionItem)
        }

        val savedItems = transactionItemRepository.saveAll(transactionItems)

        user.balance = user.balance.subtract(totalAmount)
        userRepository.save(user)

        return BuyResponse.toResponse(savedTransaction, savedItems)
    }

    override fun getUserBuyHistory(userId: Long): List<BuyItemResponse> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException()
        }
        return transactionItemRepository.findByTransactionUserId(userId).map {
            BuyItemResponse.toResponse(it)
        }
    }

    override fun getTransactionItems(transactionId: Long): List<TransactionItemResponse> {
        if (transactionRepository.findByIdAndDeletedFalse(transactionId) == null) {
            throw TransactionNotFoundException()
        }
        return transactionItemRepository.findByTransactionId(transactionId).map {
            TransactionItemResponse.toResponse(it)
        }
    }

    override fun getAllTransactions(): List<TransactionResponse> {
        return transactionRepository.findByDeletedFalseOrderByDateDesc().map {
            TransactionResponse.toResponse(it)
        }
    }

    override fun getTransactionProducts(transactionId: Long): List<TransactionItemResponse> {
        return transactionItemRepository.findByTransactionId(transactionId).map {
            TransactionItemResponse.toResponse(it)
        }
    }
}





interface ProductService {
    fun searchProducts(keyword: String): List<ProductResponse>
    fun getProductsByCategory(categoryId: Long): List<ProductResponse>
    fun getAllAvailableProducts(): List<ProductResponse>
    fun deleteProduct(id: Long)
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {

    override fun searchProducts(keyword: String): List<ProductResponse> {
        return productRepository.searchAvailableProducts(keyword).map {
            ProductResponse.toResponse(it)
        }
    }

    override fun getProductsByCategory(categoryId: Long): List<ProductResponse> {
        return productRepository.findAvailableProductsByCategory(categoryId).map {
            ProductResponse.toResponse(it)
        }
    }

    override fun getAllAvailableProducts(): List<ProductResponse> {
        return productRepository.findByCountGreaterThanAndDeletedFalse(0).map {
            ProductResponse.toResponse(it)
        }
    }
    override fun deleteProduct(id: Long) {
        productRepository.trash(id) ?: throw ProductNotFoundException()
    }
}














interface CategoryService {
    fun getAllCategories(pageable: Pageable): Page<CategoryResponse>
    fun getOne(id: Long): CategoryResponse
    fun add(request: CategoryRequest)
}

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
//    private val productRepository: ProductRepository,
) : CategoryService {
    override fun getAllCategories(pageable: Pageable): Page<CategoryResponse> {
//        productRepository.findAll().map {
//            ProductResponse(it.id!!, it.name.localized(), )
//        }
        return categoryRepository.findAll(pageable).map {
            CategoryResponse.toAdminResponse(it)
        }
    }

    override fun getOne(id: Long): CategoryResponse {
        return categoryRepository.findByIdAndDeletedFalse(id)?.let {
            CategoryResponse.toAdminResponse(it)
        } ?: throw CategoryNotFoundException()
    }

    override fun add(request: CategoryRequest) {
        categoryRepository.save(Category(
            request.name,
            request.order,
            request.description
        ))
    }

}

