package org.user.repository;

import org.user.model.User;

public interface UserRepository {

    public String addUser(String name, String aadhar);
    public User getUser(String id);
    public boolean updateUserName(String id, String name);
    public boolean updateUserAadhar(String id, String aadhar);
}
