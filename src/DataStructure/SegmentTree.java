package DataStructure;

/**
 * 线段树
 * 支持单点累加，区间求和
 */
public class SegmentTree {
    Node root;
    int minIndex;
    int maxIndex;
    private static final long zeroValue = 0;


    public SegmentTree(int minIndex, int maxIndex) {
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        root = new Node(minIndex, maxIndex, zeroValue);
    }

    public void add(int index, long newValue) {
        add(root, index, newValue);
    }

    /**
     * 单点累加，遍历到叶子节点。总和发生变化的区间需要更新segmentSum
     * index不存在时要创建节点
     * @param node
     * @param index
     * @param addition
     */
    public long add(Node node, int index, long addition) {
        //System.out.println(node);
        if (node.start > index || node.end < index) {
            return 0;
        }
        if (node.start == node.end) {
            node.setSegmentSum(node.getSegmentSum() + addition);
            return node.getSegmentSum();
        }
        int mid = getMid(node.start, node.end);
        if (node.leftChild == null) {
            node.leftChild = new Node(node.start, mid, zeroValue);
        }
        if (node.rightChild == null) {
            node.rightChild = new Node(mid+1, node.end, zeroValue);
        }
        long sum1 = 0;
        if (node.leftChild.end >= index) {
            sum1 = add(node.leftChild, index, addition) + node.rightChild.getSegmentSum();
        } else {
            sum1 = node.leftChild.getSegmentSum() + add(node.rightChild, index, addition);
        }
        node.setSegmentSum(sum1);
        return sum1;
    }

    public long query(int l, int r) {
        return query(root, l, r);
    }

    public long query(Node node, int queryLeft, int queryRight) {
        if (node == null || node.start > queryRight || node.end < queryLeft) {
            return zeroValue;
        }
        if (node.start >= queryLeft && node.end <= queryRight) {//整个区间完全匹配查询条件
            return node.getSegmentSum();
        }
        long sum = zeroValue;
        Node leftChild = node.leftChild;
        sum += query(leftChild, queryLeft, queryRight);

        Node rightChild = node.rightChild;
        sum += query(rightChild, queryLeft, queryRight);

        return sum;
    }

    /**
     * start和end范围允许为负数时，用(start+end)/2会出错
     * 例如start=-1,end=0,则mid=(-1+0)/2=0,无法使mid返回-1,代码无限递归导致栈溢出
     * @param start
     * @param end
     * @return
     */
    private int getMid(int start, int end) {
        return start + (end - start) / 2;
    }
}
class Node {
    int start;
    int end;
    private long segmentSum;

    Node leftChild;
    Node rightChild;


    public Node(int start, int end, long segmentSum) {
        this.start = start;
        this.end = end;
        this.segmentSum = segmentSum;
    }

    public long getSegmentSum() {
        return segmentSum;
    }

    public void setSegmentSum(long segmentSum) {
        this.segmentSum = segmentSum;
    }

    @Override
    public String toString() {
        return "Node{" +
                "start=" + start +
                ", end=" + end +
                ", segmentSum=" + segmentSum;
    }
}