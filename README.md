# Interoute

What is Interoute? Interoute is a simple interface call routing framework.

## Why Route interface calls?

Interface routing could be used to route interface methods to REST requests, see [Interoute Spring](https://github.com/JonathanxD/InterouteSpring) for more information about routing to REST requests.

## Simple routing example

```java
@RouterInterface(RestBackend.class)
public interface UserService {
    @RouteTo("/user/create")
    Route<User> createUser(UserForm userForm);
}

@Controller
public class UserController {
    private UserService userService;
    private ErrorHandler errorHandler;

    @Autowired
    public UserController(UserService userService, ErrorHandler errorHandler) {
        this.userService = userService;
        this.errorHandler = errorHandler;
    }

    @RequestMapping("/user/submit")
    public Response<User> create(UserForm form) {
        return this.userService.createUser(form)
                               .execute()
                               .get()
                               .map(Response::success, errorHandler::handle);
    }
}
```

### RouterInterface

Marks an `interface` as a `Router`. The value inside the annotation is the backend to use to generate routing logic.

### RouteTo

Specifies the target of routing, the notation of the value of the annotation depends on the `backend` specification.

### Route

The type of value returned by routing methods. Calling a routing method does not necessarily executes the routing login, to execute routing logic you should invoke `Route.execute()`.

Routing methods must only return either `Route<R>` or `void`. When `void` is used, `Route.execute` and `CompletableFuture.get` is invoked inside generated method implementation, implying in locking the flow until routing logic finishes.

### Backend

This is the core of Interoute, the backend generates the router interface implementation and routing methods implementation. Interoute only provides specification and common utilities, the backend is the guy which do the magic.

Interoute project has a default backend shipped with it, the default backend only delegates invocations to other instances and is a good example of how to write backend code.
