package mint.inference.gp.tree;

import mint.inference.evo.Chromosome;
import mint.inference.gp.Generator;
import mint.inference.gp.tree.nonterminals.booleans.RootBoolean;
import mint.inference.gp.tree.nonterminals.doubles.RootDouble;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;
import mint.inference.gp.tree.nonterminals.strings.AssignmentOperator;
import mint.tracedata.types.VariableAssignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in a GP tree.
 *
 * If a GP tree is to be associated with a memory, the setMemory
 * method must only be called after the tree has been completed.
 *
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class Node<T extends VariableAssignment<?>> implements Chromosome{

    protected static int ids = 0;

    protected int id;

    protected AssignmentOperator def;

    protected NonTerminal<?> parent;

    protected Set vals = new HashSet();

    public Node(){
        id = ids++;

    }

    public NonTerminal<?> getParent() {
        return parent;
    }

    public AssignmentOperator getDef() {
        return def;
    }

    public void setDef(AssignmentOperator def) {
        this.def = def;
    }

    public abstract void simplify();


    public void reset(){
        for(Node child : getChildren()){
            child.reset();
        }
    }

    public abstract boolean accept(NodeVisitor visitor) throws InterruptedException;

    protected void setParent(NonTerminal<?> parent){
        this.parent = parent;
    }

    public abstract List<Node<?>> getChildren();

    public abstract T evaluate() throws InterruptedException;


    public abstract Node<T> copy();

    public abstract void mutate(Generator g, int depth);

    public boolean swapWith(Node<?> alternative){
        assert(!(this instanceof RootDouble));
        assert(!(this instanceof RootBoolean));
        assert(!(this instanceof RootListNonTerminal));
        if(parent == null) {
            return false;
        }
        if(!alternative.getType().equals(getType()))
            return false;
        int thisIndex = parent.getChildren().indexOf(this);
        parent.getChildren().set(thisIndex,alternative);
        alternative.setParent(parent);
        return true;
    }

    public abstract String getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (id != node.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public abstract int numVarsInTree();

    public abstract int size();

    /**
     * Returns the depth of this specific node within the tree.
     * @return
     */
    public int depth(){
        if(parent == null)
            return 0;
        else
            return 1+parent.depth();
    }

    protected void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()){
            throw new InterruptedException();
        }

    }

    /**
     * Returns the maximum depth of the subtree of which this node is the
     * root.
     * @return
     */
    public int subTreeMaxdepth(){
        int maxDepth = 0;
        if(getChildren().isEmpty())
            maxDepth = depth();
        else{

            for(Node<?> child: getChildren()){
                int childMaxDepth = child.subTreeMaxdepth();
                if(childMaxDepth > maxDepth){
                    maxDepth = childMaxDepth;
                }
            }

        }
         return maxDepth;
    }

}
