package co.id.controller.layout;

import co.id.model.User;

public class Session {
    
    private static User currentUser;
    
    private Session(){
        // Mencegah instansiasi
    }
    
    public static void setCurrentUser(User user){
        currentUser = user;
    }
    
    public static User getCurrentUser(){
        return currentUser;
    }
    
    public static void clear(){
        currentUser = null;
    }
    
    public static boolean isLoggedIn(){
        return currentUser != null;
    }
    
    public static boolean isAdmin(){
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}