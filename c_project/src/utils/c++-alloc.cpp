#include <cstdlib>
#include <new>

/**
 * Allocator for the new operator
 * Allows creating dynamic object without having to use malloc
 *
 * Not used anywhere in the current project
 */

using std::new_handler;
new_handler __new_handler;

void *
operator new(std::size_t sz) {
    void *p;

    /* malloc (0) is unpredictable; avoid it.  */
    if (sz == 0)
        sz = 1;
    p = (void *) malloc(sz);
    while (p == 0) {
        new_handler handler = __new_handler;
        if (!handler)
            std::abort();
        handler();
        p = (void *) std::malloc(sz);
    }

    return p;
}

void *
operator new[](std::size_t sz) {
    return ::operator new(sz);
}

void
operator delete(void *ptr) {
    if (ptr)
        std::free(ptr);
}

new_handler
std::set_new_handler(new_handler handler) {
    new_handler prev_handler = __new_handler;
    __new_handler = handler;
    return prev_handler;
}