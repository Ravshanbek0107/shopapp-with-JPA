package uz.zero.shopapp

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.Locale

@ControllerAdvice
class ExceptionHandler(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(Throwable::class)
    fun handleOtherExceptions(exception: Throwable): ResponseEntity<Any> {
        when (exception) {
            is ShopAppException-> {

                return ResponseEntity
                    .badRequest()
                    .body(exception.getErrorMessage(errorMessageSource))
            }

            else -> {
                exception.printStackTrace()
                return ResponseEntity
                    .badRequest().body(
                        BaseMessage(100,
                            "Iltimos support bilan bog'laning")
                    )
            }
        }
    }

}



sealed class ShopAppException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments(),
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}


class CategoryNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_NOT_FOUND
}

class UserNotFoundException : ShopAppException() {
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}

class UserAlreadyExistsException : ShopAppException() {
    override fun errorType() = ErrorCode.USER_ALREADY_EXISTS
}

class InvalidFullnameException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_FULLNAME_EXCEPTION
}
class InvalidUsernameException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_USERNAME_EXCEPTION
}

class InvalidCategoryNameException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_CATEGORYNAME_EXCEPTION
}
class InvalidOrderException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_ORDER_EXCEPTION
}
class InvalidLocalizedNameException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_LOCALIZEDNAME_EXCEPTION
}

class ProductNotFoundException : ShopAppException() {
    override fun errorType() = ErrorCode.PRODUCT_NOT_FOUND
}

class TransactionNotFoundException : ShopAppException() {
    override fun errorType() = ErrorCode.TRANSACTION_NOT_FOUND
}

class InsufficientBalanceException : ShopAppException() {
    override fun errorType() = ErrorCode.INSUFFICIENT_BALANCE
}

class InsufficientProductException : ShopAppException() {
    override fun errorType() = ErrorCode.INSUFFICIENT_PRODUCT
}
class InvalidAmountException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_AMOUNT
}