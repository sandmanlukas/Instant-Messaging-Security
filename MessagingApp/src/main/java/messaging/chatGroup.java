import java.util.ArrayList;

public class chatGroup {

    private final ArrayList<String> members;
    private final boolean creator;

    public chatGroup(boolean creator) {
        members = new ArrayList<>();
        this.creator = creator;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public boolean getMember(String memberName) {
        return members.contains(memberName);
    }

    public boolean getCreator() {
        return creator;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public void removeMember(String member) {
        members.remove(member);
    }
}
