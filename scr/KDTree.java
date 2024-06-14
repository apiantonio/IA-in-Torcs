package  scr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/*
KD-Tree for an efficient implementation of the K-NN.
Do not touch!

CLASSE ADATTATA ALLA NOSTRA IMPLEMENTAZIONE DI SAMPLE e dunque alle nostre features
*/
class KDTree {

    private final KDNode root;

    public KDTree(List<Sample> points) {
        root = buildTree(points, 0);
    }

    private static class KDNode {
        Sample point;
        KDNode left, right;

        KDNode(Sample point) {
            this.point = point;
        }
    }

    private KDNode buildTree(List<Sample> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % 12; // 12 features in un Sample
        points.sort(Comparator.comparingDouble(p -> getCoordinate(p, axis)));
        int medianIndex = points.size() / 2;
        KDNode node = new KDNode(points.get(medianIndex));

        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }

    private double getCoordinate(Sample sample, int axis) {
        switch (axis) {
            case 0: return sample.getAngleToTrackAxis();
            case 1: return sample.getTrackPosition();
            case 2: return sample.getTrackEdgeSensor10();
            case 3: return sample.getTrackEdgeSensors9();
            case 4: return sample.getTrackEdgeSensors8();
            case 5: return sample.getRpm();
            case 6: return sample.getGear();
            case 7: return sample.getSteering();
            case 8: return sample.getAccelerate();
            case 9: return sample.getBrake();
            case 10: return sample.getClutch();
            default: return sample.getCls();
        }
    }

    public List<Sample> kNearestNeighbors(Sample target, int k) {
        PriorityQueue<Sample> pq = new PriorityQueue<>(k, Comparator.comparingDouble(target::distance).reversed());
        kNearestNeighbors(root, target, k, 0, pq);
        return new ArrayList<>(pq);
    }

    private void kNearestNeighbors(KDNode node, Sample target, int k, int depth, PriorityQueue<Sample> pq) {
        if (node == null) {
            return;
        }

        double distance = target.distance(node.point);
        if (pq.size() < k) {
            pq.offer(node.point);
        } else if (distance < target.distance(pq.peek())) {
            pq.poll();
            pq.offer(node.point);
        }

        int axis = depth % 12;
        KDNode nearNode = (getCoordinate(target, axis) < getCoordinate(node.point, axis)) ? node.left : node.right;
        KDNode farNode = (nearNode == node.left) ? node.right : node.left;

        kNearestNeighbors(nearNode, target, k, depth + 1, pq);

        if (pq.size() < k || Math.abs(getCoordinate(target, axis) - getCoordinate(node.point, axis)) < target.distance(pq.peek())) {
            kNearestNeighbors(farNode, target, k, depth + 1, pq);
        }
    }
}
