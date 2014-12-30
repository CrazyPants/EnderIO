package crazypants.enderio.machine.transceiver;

import net.minecraft.nbt.NBTTagCompound;


public class Channel {
 
  public static Channel readFromNBT(NBTTagCompound root) {
    if(!root.hasKey("name")) {
      return null;
    }
    String name = root.getString("name");
    String user = null;
    if(root.hasKey("user")) {
      user = root.getString("user");
    }
    ChannelType type = ChannelType.values()[root.getShort("type")];
    return new Channel(name, user, type);
  }
  
  private final String name;
  final String user;
  final ChannelType type;

  public Channel(String name, String user, ChannelType type) {
    this.name = trim(name);
    this.user = user;
    this.type = type;
  }

  public boolean isPublic() {
    return user == null;
  }

  public void writeToNBT(NBTTagCompound root) {
    if(name == null || name.isEmpty()) {
      return;
    } 
    root.setString("name", name);
    root.setShort("type", (short)type.ordinal());
    if(user != null && user.length() > 0) {
      root.setString("user", user);
    }        
  }
  
  private String trim(String str) {
    if(str == null) {
      return null;
    }
    str = str.trim();
    if(str.isEmpty()) {
      return null;
    }
    return str;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    Channel other = (Channel) obj;
    if(getName() == null) {
      if(other.getName() != null)
        return false;
    } else if(!getName().equals(other.getName()))
      return false;
    if(type != other.type)
      return false;
    if(user == null) {
      if(other.user != null)
        return false;
    } else if(!user.equals(other.user))
      return false;
    return true;
  }

  public String getName() {
    return name;
  }

  public ChannelType getType() {
    return type;
  }

  public String getUser() {
    return user;
  }

  @Override
  public String toString() {
    return "Channel [name=" + name + ", user=" + user + ", type=" + type + "]";
  }

}
