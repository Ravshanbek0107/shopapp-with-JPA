package uz.zero.shopapp

import org.springframework.stereotype.Service
import java.math.BigDecimal

interface UserService {
    fun createUser(request: UserCreateRequest): UserResponse
    fun getUserById(id: Long): UserResponse
    fun getAllUsers(): List<UserResponse>
    fun deleteUser(id: Long)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun createUser(request: UserCreateRequest): UserResponse {
        if (request.fullname.isBlank()) throw InvalidFullnameException()
        if (request.username.isBlank() || request.username.length <= 3) throw InvalidUsernameException()

        if (userRepository.existsByUsername(request.username)) {
            throw UserAlreadyExistsException()
        }

        val user = User(
            fullname = request.fullname.trim(),
            username = request.username.trim(),
            balance = BigDecimal.ZERO
        )

        val savedUser = userRepository.save(user)
        return UserResponse.toResponse(savedUser)
    }

    override fun getUserById(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        return UserResponse.toResponse(user)
    }

    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAllNotDeleted().map { user ->
            UserResponse.toResponse(user)
        }
    }


    override fun deleteUser(id: Long) {
        userRepository.trash(id) ?: throw UserNotFoundException()
    }


}

interface UserPaymentService {
    fun addBalance(request: UserPaymentRequest): UserPaymentResponse
    fun getUserPaymentHistory(userId: Long): List<UserPaymentHistoryResponse>
}


@Service
class UserPaymentServiceImpl(
    private val userRepository: UserRepository,
    private val userPaymentTransactionRepository: UserPaymentTransactionRepository
) : UserPaymentService {

    override fun addBalance(request: UserPaymentRequest): UserPaymentResponse {
        val user = userRepository.findByIdAndDeletedFalse(request.userId) ?: throw UserNotFoundException()

        if (request.amount <= BigDecimal.ZERO) throw InvalidAmountException()


        user.balance = user.balance.add(request.amount)
        userRepository.save(user)

        val paymentTransaction = UserPaymentTransaction(
            user = user,
            amount = request.amount
        )

        val savedPayment = userPaymentTransactionRepository.save(paymentTransaction)
        return UserPaymentResponse.toResponse(savedPayment)
    }

    override fun getUserPaymentHistory(userId: Long): List<UserPaymentHistoryResponse> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException()
        }

        return userPaymentTransactionRepository.findAllNotDeleted()
            .filter { it.user.id == userId }
            .map { UserPaymentHistoryResponse.toResponse(it) }
    }
}

interface CategoryService {
    fun createCategory(request: CategoryCreateRequest): CategoryResponse
    fun getAllCategories(): List<CategoryResponse>
    fun updateCategory(id: Long, request: CategoryUpdateRequest): CategoryResponse
    fun deleteCategory(id: Long)
}

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository
) : CategoryService {

    override fun createCategory(request: CategoryCreateRequest): CategoryResponse {
        if (request.name.isBlank()) throw InvalidCategoryNameException()
        if (request.order < 0) throw InvalidOrderException()

        val category = Category(
            name = request.name.trim(),
            order = request.order,
        )

        val savedCategory = categoryRepository.save(category)
        return CategoryResponse.toResponse(savedCategory)
    }

    override fun getAllCategories(): List<CategoryResponse> {
        return categoryRepository.findAllNotDeleted().map { category ->
            CategoryResponse.toResponse(category)
        }
    }


    override fun updateCategory(id: Long, request: CategoryUpdateRequest): CategoryResponse {
        val category = categoryRepository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFoundException()

        request.name?.let {
            if (it.isBlank()) throw InvalidCategoryNameException()
            category.name = it.trim()
        }

        request.order?.let {
            if (it < 0) throw InvalidOrderException()
            category.order = it
        }

        val updatedCategory = categoryRepository.save(category)
        return CategoryResponse.toResponse(updatedCategory)
    }

    override fun deleteCategory(id: Long) {
        categoryRepository.trash(id) ?: throw CategoryNotFoundException()
    }
}

interface ProductService {
    fun createProduct(request: ProductCreateRequest): ProductResponse
    fun getProductById(id: Long): ProductResponse
    fun getAllProducts(): List<ProductResponse>
    fun updateProduct(id: Long, request: ProductUpdateRequest): ProductResponse
    fun deleteProduct(id: Long)
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ProductService {

    override fun createProduct(request: ProductCreateRequest): ProductResponse {
        validateLocalizedName(request.name)
        if (request.count < 0) throw InvalidAmountException()
        if (request.amount <= BigDecimal.ZERO) throw InvalidAmountException()

        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId) ?: throw CategoryNotFoundException()

        val product = Product(
            name = LocalizedName(request.name.uz, request.name.ru, request.name.en),
            amount = request.amount,
            count = request.count,
            category = category
        )

        val savedProduct = productRepository.save(product)
        return ProductResponse.toResponse(savedProduct)
    }

    override fun getProductById(id: Long): ProductResponse {
        val product = productRepository.findByIdAndDeletedFalse(id) ?: throw ProductNotFoundException()
        return ProductResponse.toResponse(product)
    }

    override fun getAllProducts(): List<ProductResponse> {
        return productRepository.findAllNotDeleted().map { product ->
            ProductResponse.toResponse(product)
        }
    }


    override fun updateProduct(id: Long, request: ProductUpdateRequest): ProductResponse {
        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw ProductNotFoundException()

        request.name?.let {
            validateLocalizedName(it)
            product.name = LocalizedName(it.uz, it.ru, it.en)
        }

        request.count?.let {
            if (it < 0) throw InvalidAmountException()
            product.count = it
        }

        request.amount?.let {
            if (it <= BigDecimal.ZERO) throw InvalidAmountException()
            product.amount = it
        }

        request.categoryId?.let { categoryId ->
            val category = categoryRepository.findByIdAndDeletedFalse(categoryId) ?: throw CategoryNotFoundException()
            product.category = category
        }

        val updatedProduct = productRepository.save(product)
        return ProductResponse.toResponse(updatedProduct)
    }

    override fun deleteProduct(id: Long) {
        productRepository.trash(id) ?: throw ProductNotFoundException()
    }

    private fun validateLocalizedName(name: LocalizedNameRequest) {
        if (name.uz.isBlank() || name.ru.isBlank() || name.en.isBlank()) throw InvalidLocalizedNameException()

    }
}


interface TransactionService {
    fun processBuy(buyRequest: BuyRequest): BuyResponse
    fun getUserBuyHistory(userId: Long): List<UserBuyHistoryResponse>
    fun getTransactionItems(transactionId: Long): List<TransactionItemResponse>
    fun getAllTransactions(): List<AllTransactionsResponse>
}



@Service
class TransactionServiceImpl(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionItemRepository: TransactionItemRepository
) : TransactionService {

    override fun processBuy(buyRequest: BuyRequest): BuyResponse {

        val user = userRepository.findByIdAndDeletedFalse(buyRequest.userId) ?: throw UserNotFoundException()

        var totalAmount = BigDecimal.ZERO

        for (item in buyRequest.items) {
            if (item.count <= 0) throw InvalidAmountException()

            val product = productRepository.findByIdAndDeletedFalse(item.productId) ?: throw ProductNotFoundException()

            if (product.count < item.count) throw InsufficientProductException()

            val itemTotal = product.amount.multiply(BigDecimal(item.count))
            totalAmount = totalAmount.add(itemTotal)
        }

        if (user.balance < totalAmount) throw InsufficientBalanceException()


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

            val itemTotal = product.amount.multiply(BigDecimal(item.count))

            val transactionItem = TransactionItem(
                product = product,
                count = item.count,
                amount = product.amount,
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

    override fun getUserBuyHistory(userId: Long): List<UserBuyHistoryResponse> {
        if (!userRepository.existsById(userId)) throw UserNotFoundException()


        val transactions = transactionRepository.findAllNotDeleted()
            .filter { it.user.id == userId }

        return transactions.map { transaction ->
            val items = transactionItemRepository.findAllNotDeleted()
                .filter { it.transaction.id == transaction.id }
            UserBuyHistoryResponse.toResponse(transaction, items)
        }
    }

    override fun getTransactionItems(transactionId: Long): List<TransactionItemResponse> {
        if (transactionRepository.findByIdAndDeletedFalse(transactionId) == null) {
            throw TransactionNotFoundException()
        }

        return transactionItemRepository.findAllNotDeleted()
            .filter { it.transaction.id == transactionId }
            .map { TransactionItemResponse.toResponse(it) }
    }

    override fun getAllTransactions(): List<AllTransactionsResponse> {
        return transactionRepository.findAllNotDeleted().map { transaction ->
            AllTransactionsResponse(
                transactionId = transaction.id!!,
                user = UserResponse.toResponse(transaction.user),
                totalAmount = transaction.totalAmount,
                date = transaction.date
            )
        }
    }
}
















