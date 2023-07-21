import java.util.*;

public class Ex10_20220808003 {
    public static void main(String[] args) {

        User user1 = SocialNetwork.register("Alisa", "alisa@hotmail.com");
        User user2 = SocialNetwork.register("Lexus", "Lexus@hotmail.com");
        User user3 = SocialNetwork.register("Emerenta", "emerenta@hotmail.com");

        user1.follow(user2);

        Post post1 = user2.post("Yes I like drinking coffee near a window looking sad");

        user1.like(post1);

        Comment comment1 = user3.comment(post1, "rad bruh");

        Post post2 = user2.post("Gonna beat the raid boss tonight babbyy");

        user3.follow(user2);

        Post post3 = user2.post("Y'all I'mma ride mine horse tanight");

        user1.message(user2, "sup?");

        user2.read(user1);

        Set<Post> feed = SocialNetwork.getFeed(user2);

        System.out.println("User 2's Feed:");
        for (Post post : feed) {
            System.out.println(post.getContent());
        }

        System.out.println("\nSearch Results for 'e':");
        Map<User, String> searchResults = SocialNetwork.search("e");
        for (Map.Entry<User, String> entry : searchResults.entrySet()) {
            User user = entry.getKey();
            String username = entry.getValue();
            System.out.println("User: " + username + ", Email: " + user.getEmail());
        }
    }
}

class User {
    private int id;
    private String username;
    private String email;
    private Set<User> followers;
    private Set<User> following;
    private Set<Post> likedPosts;
    private Map<User, Queue<Message>> messages;
    private List<Post> posts;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.id = hashCode();
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.likedPosts = new HashSet<>();
        this.messages = new HashMap<>();
        this.posts = new ArrayList<>();
    }

    public Map<User, Queue<Message>> getMessages() {
        return messages;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public Set<Post> getLikedPosts() {
        return likedPosts;
    }

    public void message(User recipient, String content) {
        messages.putIfAbsent(recipient, new LinkedList<>());
        recipient.getMessages().putIfAbsent(this, new LinkedList<>());

        Message message = new Message(this, content);
        messages.get(recipient).add(message);
        recipient.getMessages().get(this).add(message);

        read(this);
    }

    public void read(User user) {
        if (messages.containsKey(user)) {
            Queue<Message> userMessages = messages.get(user);
            for (Message message : userMessages) {
                System.out.println(message.read(this));
                message.setSeen(true);
            }
        }
    }

    public void follow(User user) {
        if (!following.contains(user)) {
            following.add(user);
            user.followers.add(this);
        } else {
            following.remove(user);
        }
    }

    public void like(Post post) {
        if (!likedPosts.contains(post)) {
            likedPosts.add(post);
            post.likedBy(this);
        } else {
            likedPosts.remove(post);
        }
    }

    public Post post(String content) {
        return SocialNetwork.post(this, content);
    }

    public Comment comment(Post post, String content) {
        Comment comment = new Comment(content);
        post.commentBy(this, comment);
        return comment;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        return email.equals(other.email);
    }

    public int hashCode() {
        return Objects.hash(email);


    }
}

class Message {
    private boolean seen;
    private java.util.Date dateSent;
    private String content;
    private User sender;

    public Message(User sender, String content){
        this.sender = sender;
        this.content = content;
        this.dateSent = new Date();
        this.seen = false;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String read(User reader) {
        if (sender != reader) {
            seen = true;
        }
        return "Sent at: " + dateSent + "\n" + content;
    }

    public boolean hasRead() {
        return seen;
    }
}

class Post{
    private java.util.Date datePosted;
    private String content;
    private Set<User> likes;
    private Map<User, List<Comment>> comments;

    public Post(String content) {
        this.content = content;
        this.datePosted = new Date();
        this.likes = new HashSet<>();
        this.comments = new HashMap<>();
    }
    public boolean likedBy(User user) {
        if (likes.add(user)) {
            return true;
        } else {
            likes.remove(user);
            return false;
        }
    }

    public boolean commentBy(User user, Comment comment) {
        List<Comment> userComments = comments.computeIfAbsent(user, k -> new ArrayList<>());
        return userComments.add(comment);
    }

    public String getContent() {
        return "Posted at: " + datePosted + "\n" + content;
    }

    public Comment getComment(User user, int index) {
        List<Comment> userComments = comments.get(user);
        if (userComments != null && index >= 0 && index < userComments.size()) {
            return userComments.get(index);
        }
        return null;
    }

    public int getCommentCount() {
        int count = 0;
        for (List<Comment> userComments : comments.values()) {
            count += userComments.size();
        }
        return count;
    }

    public int getCommentCountByUser(User user) {
        List<Comment> userComments = comments.get(user);
        return (userComments != null) ? userComments.size() : 0;
    }

}

class Comment extends Post{
    public Comment(String content) {
        super(content);
    }
}

class SocialNetwork{
    private static Map<User, List<Post>> postsByUsers = new HashMap<>();

    public static User register(String username, String email) {
        User user = new User(username, email);
        if (!postsByUsers.containsKey(user)) {
            postsByUsers.put(user, new ArrayList<>());
            return user;
        } else {
            return null;
        }
    }

    public static Post post(User user, String content) {
        if (postsByUsers.containsKey(user)) {
            Post post = new Post(content);
            postsByUsers.get(user).add(post);
            return post;
        } else {
            return null;
        }
    }

    public static User getUser(String email) {
        int hashedEmail = Objects.hash(email);
        for (User user : postsByUsers.keySet()) {
            if (user.getId() == hashedEmail) {
                return user;
            }
        }
        return null;
    }

    public static Set<Post> getFeed(User user) {
        Set<Post> feed = new HashSet<>();
        Set<User> following = user.getFollowing();
        for (User followingUser : following) {
            List<Post> userPosts = postsByUsers.get(followingUser);
            if (userPosts != null) {
                feed.addAll(userPosts);
            }
        }
        feed.addAll(postsByUsers.get(user));
        return feed;
    }

    public static Map<User, String> search(String keyword) {
        Map<User, String> searchResults = new HashMap<>();
        for (User user : postsByUsers.keySet()) {
            if (user.getUsername().contains(keyword)) {
                searchResults.put(user, user.getUsername());
            }
        }
        return searchResults;
    }

    public static <K, V> Map<V, Set<K>> reverseMap(Map<K, V> map) {
        Map<V, Set<K>> reversedMap = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            reversedMap.computeIfAbsent(value, k -> new HashSet<>()).add(key);
        }
        return reversedMap;

    }
}