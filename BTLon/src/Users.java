public class Users {
    public String name = "default";
    protected String username = "default";
    private String password = "default";

    public String getUsername ()
    {
        return this.username;
    }

    public boolean checkLogin (String password) {
        return (password == this.password);
    }

    public void changePassword (String new_password)
    {
        this.password = new_password;
    }

    public void main ()
    {
        System.out.println (name);
        System.out.println (checkLogin ("default"));
    }
}
