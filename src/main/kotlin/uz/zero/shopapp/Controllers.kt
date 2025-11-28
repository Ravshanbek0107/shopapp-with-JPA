package uz.zero.shopapp

import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController




@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(@RequestBody request: UserCreateRequest): UserResponse {
        return userService.createUser(request)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        return userService.getUserById(id)
    }

    @GetMapping
    fun getAllUsers(): List<UserResponse> {
        return userService.getAllUsers()
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) {
        userService.deleteUser(id)
    }
}

@RestController
@RequestMapping("/api/payments")
class UserPaymentController(
    private val userPaymentService: UserPaymentService
) {

    @PostMapping("/add-balance")
    fun addBalance(@RequestBody request: UserPaymentRequest): UserPaymentResponse {
        return userPaymentService.addBalance(request)
    }

    @GetMapping("/user/{userId}/history")
    fun getUserPaymentHistory(@PathVariable userId: Long): List<UserPaymentHistoryResponse> {
        return userPaymentService.getUserPaymentHistory(userId)
    }
}

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    fun createCategory(@RequestBody request: CategoryCreateRequest): CategoryResponse {
        return categoryService.createCategory(request)
    }

    @GetMapping
    fun getAllCategories(): List<CategoryResponse> {
        return categoryService.getAllCategories()
    }

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody request: CategoryUpdateRequest): CategoryResponse {
        return categoryService.updateCategory(id, request)
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long) {
        categoryService.deleteCategory(id)
    }
}

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    fun createProduct(@RequestBody request: ProductCreateRequest): ProductResponse {
        return productService.createProduct(request)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ProductResponse {
        return productService.getProductById(id)
    }

    @GetMapping
    fun getAllProducts(): List<ProductResponse> {
        return productService.getAllProducts()
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Long, @RequestBody request: ProductUpdateRequest): ProductResponse {
        return productService.updateProduct(id, request)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long) {
        productService.deleteProduct(id)
    }
}


@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/buy")
    fun processBuy(@RequestBody request: BuyRequest): BuyResponse {
        return transactionService.processBuy(request)
    }

    @GetMapping("/user/{userId}/history")
    fun getUserBuyHistory(@PathVariable userId: Long): List<UserBuyHistoryResponse> {
        return transactionService.getUserBuyHistory(userId)
    }

    @GetMapping("/{transactionId}/items")
    fun getTransactionItems(@PathVariable transactionId: Long): List<TransactionItemResponse> {
        return transactionService.getTransactionItems(transactionId)
    }

    @GetMapping("/admin/getall")
    fun getAllTransactions(): List<AllTransactionsResponse> {
        return transactionService.getAllTransactions()
    }
}
