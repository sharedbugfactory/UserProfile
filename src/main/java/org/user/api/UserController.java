package org.user.api;

import org.springframework.lang.Nullable;
import org.user.api.requestDto.CreateUserRequest;
import org.user.api.requestDto.UpdateUserAadharRequest;
import org.user.api.requestDto.UpdateUserNameRequest;
import org.user.api.responseDto.CreateUserResponse;
import org.user.api.responseDto.UpdateUserResponse;
import jakarta.validation.Valid;
import org.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.user.router.UserRepoRouter;


@RestController
@RequestMapping("/org/user/api/users")
public class UserController {

    private final UserRepoRouter router;

    public UserController(UserRepoRouter router) {
        this.router = router;
    }

    // Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse add(@RequestParam(name = "db", required = false) @Nullable String db, @Valid @RequestBody CreateUserRequest request) {
        var repo = router.pick(db);
        String id = repo.addUser(request.getName(), request.getAadharNumber());
        return new CreateUserResponse(id);
    }

    @GetMapping("/{id}")
    public User get(@RequestParam(name = "db", required = false) @Nullable String db,
                    @PathVariable String id) {
        return router.pick(db).getUser(id);
    }

    // Update name
    @PatchMapping("/{id}/name")
    public UpdateUserResponse updateName(@RequestParam(name = "db", required = false) @Nullable String db,
                                         @PathVariable String id,
                                         @Valid @RequestBody UpdateUserNameRequest request) {
        boolean updated = router.pick(db).updateUserName(id, request.getName());
        return new UpdateUserResponse(updated);
    }

    // Update aadhar
    @PatchMapping("/{id}/aadhar")
    public UpdateUserResponse updateAadhar(@RequestParam(name = "db", required = false) @Nullable String db,
                                           @PathVariable String id,
                                           @Valid @RequestBody UpdateUserAadharRequest request) {
        boolean updated = router.pick(db).updateUserAadhar(id, request.getAadharNumber());
        return new UpdateUserResponse(updated);
    }
}
