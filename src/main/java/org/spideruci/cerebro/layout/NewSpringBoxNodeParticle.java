package org.spideruci.cerebro.layout;

import java.util.Iterator;

import org.graphstream.ui.geom.Vector3;
import org.graphstream.ui.layout.springbox.EdgeSpring;
import org.graphstream.ui.layout.springbox.Energies;
import org.graphstream.ui.layout.springbox.NodeParticle;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.layout.springbox.implementations.SpringBoxNodeParticle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.geom.Point3;

public class NewSpringBoxNodeParticle extends SpringBoxNodeParticle {

	public NewSpringBoxNodeParticle(SpringBox box, String id) {
		super(box, id);
		// TODO Auto-generated constructor stub
	}

	public NewSpringBoxNodeParticle(SpringBox box, String id, double x, double y, double z) {
		super(box, id, x, y, z);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void attraction(Vector3 delta) {
		NewSpringBox box = (NewSpringBox) this.box;
		boolean is3D = box.is3D();
		Energies energies = box.getEnergies();
		int neighbourCount = neighbours.size();

		for (EdgeSpring edge : neighbours) {
			if (!edge.ignored) {
				NodeParticle other = edge.getOpposite(this);
				Point3 opos = other.getPosition();

				delta.set(opos.x - pos.x, opos.y - pos.y, is3D ? opos.z - pos.z
						: 0);

				double len = delta.normalize();
//				double k = box.getK() * edge.weight;
//				double factor = box.K1 * (len - k);
				
				double factor = box.getK1() * edge.weight;
//				double factor = box.getK1() * Math.log10(edge.weight);

				// delta.scalarMult( factor );
				delta.scalarMult(factor * (1f / (neighbourCount * 0.1f)));
				// ^^^ XXX NEW inertia based on the node degree. This is one
				// of the amelioration of the Spring-Box algorithm. Compare
				// it to the Force-Atlas algorithm that does this on
				// **repulsion**.

				disp.add(delta);
				attE += factor;
				energies.accumulateEnergy(factor);
			}
		}

	}
	
	@Override
	protected void repulsionN2(Vector3 delta) {
		NewSpringBox box = (NewSpringBox) this.box;
		boolean is3D = box.is3D();
		ParticleBox nodes = box.getSpatialIndex();
		Energies energies = box.getEnergies();
		Iterator<Object> i = nodes.getParticleIdIterator();

		while (i.hasNext()) {
			NewSpringBoxNodeParticle node = (NewSpringBoxNodeParticle) nodes
					.getParticle(i.next());

			if (node != this) {
				delta.set(node.pos.x - pos.x, node.pos.y - pos.y,
						is3D ? node.pos.z - pos.z : 0);

				double len = delta.normalize();

				if(len > 0) {
					if (len < box.getK())
						len = box.getK(); // XXX NEW To prevent infinite
									// repulsion.
				
//					double factor = len != 0 ? ((box.K2 / (len * len)) * node.weight)
//						: 0.00001;
//					double factor = box.getK2() * Math.log10(node.weight);
					double factor = box.getK2() * node.weight;

					energies.accumulateEnergy(factor); // TODO check this
					delta.scalarMult(-factor);
					disp.add(delta);
				}
			}
		}
	}
	
}
