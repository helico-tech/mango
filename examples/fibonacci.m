fn fib(n) {
    when (n == 0) return 0
    when (n == 1) return 1

    return fib(n - 1) + fib(n - 2)
}

fn main() {
    return fib(30)
}