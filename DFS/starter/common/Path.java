package common;

import java.io.*;
import java.util.*;

/** Distributed filesystem paths.

    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Comparable<Path>, Serializable
{

    // Create a ArrayList<String> to store the components of a path
    public ArrayList<String> pathComponents;

    // Create a Path Separator variable
    public static final String pathSeparator = "/";
    public static final String illegalSeparator = ":";
    private static final long serialVersionUID = 10171991L;

    /** Creates a new path which represents the root directory. */
    public Path()
    {
        this.pathComponents = new ArrayList<String>();
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
        // Handle Illegal Arguments
        if (component == null) {
          throw new IllegalArgumentException("Component is null");
        }

        if (component.length() == 0) {
          throw new IllegalArgumentException("Component is empty");
        }

        if (component.indexOf(pathSeparator) != -1 || (component.indexOf(illegalSeparator) != -1)) {
          throw new IllegalArgumentException("Component includes separator / or :");
        }

        if (path == null) {
          throw new IllegalArgumentException("The exting Path is null");
        }

        // Create the ArrayList
        this.pathComponents = new ArrayList<String>();

        // Add existing path to the list
        this.pathComponents.addAll((Collection<String>)path.pathComponents);

        // Add the current component to the list
        this.pathComponents.add(component);
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
        // Handle Illegal Arguments
        if (path == null) {
          throw new IllegalArgumentException("The exting Path is null");
        }

        if (!path.startsWith(pathSeparator)) {
          throw new IllegalArgumentException("The existing path doesn't start with /");
        }

        if (path.contains(illegalSeparator)) {
          throw new IllegalArgumentException("The exiting path contains :");
        }

        // Create the ArrayList
        this.pathComponents = new ArrayList<String>();

        // Toekinze the path and add to the ArrayList
        StringTokenizer st = new StringTokenizer(path, pathSeparator);
        while (st.hasMoreTokens()) {
            this.pathComponents.add(st.nextToken());
        }
    }

    public Path(ArrayList<String> components){
    	this.pathComponents = components;
    }

    public Path(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("The exting Path is null");
        }
        this.pathComponents = new ArrayList<>();
        this.pathComponents.addAll((Collection<String>)path.pathComponents);
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        class PathIterator implements Iterator<String> {
          Iterator<String> it;

          public PathIterator() {
            it = pathComponents.iterator();
          }

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public String next() {
            return it.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException("Remove operation is not supported.");
          }
        }

        return new PathIterator();
    }

    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
      if(!directory.exists()) {
              throw new FileNotFoundException("Root directory does not exist");
      }
      if(!directory.isDirectory()) {
              throw new IllegalArgumentException("Directory exists but does not"
               + "refer to a directory");
      }

      ArrayList<Path> paths = new ArrayList<Path>();
      ArrayList<Path> resultPaths = listRec(new Path(), directory, paths);
      return resultPaths.toArray(new Path[resultPaths.size()]);
    }

    /** Helper routine to list files in a directory recursively
     */
    public static ArrayList<Path> listRec(Path p, File directory,
        ArrayList<Path> paths) throws FileNotFoundException {

        if (!directory.exists()) {
          	throw new FileNotFoundException("Directory doesn't exist");
        }
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("Directory exists but does not"
                     + "refer to a directory");
        }

        File[] files = directory.listFiles();
        for (File f : files) {
          	if (f.isFile()) {
          		paths.add(new Path(p, f.getName()));
          	} else if (f.isDirectory()) {
          		listRec(new Path (p, f.getName()), f, paths);
          	}
         }

        return paths;
    }

    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
        return this.pathComponents.isEmpty();
    }

    /** Returns the path to the parent of this path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent()
    {
        if (isRoot()) {
          throw new IllegalArgumentException("Path represents the root directory, " +
                                              "and therefore has no parent");
        }

        ArrayList<String> parentPathComponents = new ArrayList<String>();
        parentPathComponents.addAll(pathComponents);
        parentPathComponents.remove(parentPathComponents.size()-1);
        return new Path(parentPathComponents);
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
        if (this.isRoot()) {
          throw new IllegalArgumentException("Path represents the root and has no last component");
        }

        return this.pathComponents.get(pathComponents.size()-1);
    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if it is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
        if (other.pathComponents.size() > this.pathComponents.size()){
            return false;
        }
        for (int i=0; i<other.pathComponents.size(); i++){
            if (!other.pathComponents.get(i).equals(this.pathComponents.get(i))){
                return false;
            }
        }
        return true;
    }

    public String getNextComponentOf(Path longerPath){
        if (longerPath.isSubpath(this)){
            return longerPath.pathComponents.get(this.pathComponents.size());
        }
        return null;
    }

    public void removeLastComponent(){
        if (!pathComponents.isEmpty()){
            pathComponents.remove(pathComponents.size()-1);
        }
    }

    public Path getPathWithoutLastComponent(){
        Path parent = new Path(this);
        parent.removeLastComponent();
        return parent;
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
        String fullPath = root.getAbsolutePath() + this.toString();
        return new File(fullPath);
    }

    /** Compares this path to another.

        <p>
        An ordering upon <code>Path</code> objects is provided to prevent
        deadlocks between applications that need to lock multiple filesystem
        objects simultaneously. By convention, paths that need to be locked
        simultaneously are locked in increasing order.

        <p>
        Because locking a path requires locking every component along the path,
        the order is not arbitrary. For example, suppose the paths were ordered
        first by length, so that <code>/etc</code> precedes
        <code>/bin/cat</code>, which precedes <code>/etc/dfs/conf.txt</code>.

        <p>
        Now, suppose two users are running two applications, such as two
        instances of <code>cp</code>. One needs to work with <code>/etc</code>
        and <code>/bin/cat</code>, and the other with <code>/bin/cat</code> and
        <code>/etc/dfs/conf.txt</code>.

        <p>
        Then, if both applications follow the convention and lock paths in
        increasing order, the following situation can occur: the first
        application locks <code>/etc</code>. The second application locks
        <code>/bin/cat</code>. The first application tries to lock
        <code>/bin/cat</code> also, but gets blocked because the second
        application holds the lock. Now, the second application tries to lock
        <code>/etc/dfs/conf.txt</code>, and also gets blocked, because it would
        need to acquire the lock for <code>/etc</code> to do so. The two
        applications are now deadlocked.

        @param other The other path.
        @return Zero if the two paths are equal, a negative number if this path
                precedes the other path, or a positive number if this path
                follows the other path.
     */
    @Override
    public int compareTo(Path other)
    {
        return this.toString().compareTo(other.toString());
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
        if (!other.getClass().equals(Path.class)){
            return false;
        }
        return compareTo((Path)other) == 0;
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
        if (pathComponents.isEmpty()){
            return "/";
        }
        StringBuilder builder = new StringBuilder();
        for (String component: pathComponents){
            builder.append("/");
            builder.append(component);
        }
        return builder.toString();
    }
}
