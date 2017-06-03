package at.befri.graph;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class ProceduralSphere {

	private ProceduralSphere() {
	}

	public static Mesh generate(float radius, int widthSegments, int heightSegments, float phiStart, float phiLength,
			float thetaStart, float thetaLength, boolean ccw) {
		widthSegments = Math.max(3, widthSegments);
		heightSegments = Math.max(2, heightSegments);

		float thetaEnd = thetaStart + thetaLength;

		List<Vector3f> vertexList = new ArrayList<>();
		List<Vector3f> normalList = new ArrayList<>();
		List<Integer> indexList = new ArrayList<>();
		List<Vector2f> uvList = new ArrayList<>();

		int index = 0;
		List<List<Integer>> grid = new ArrayList<>();

		for (int iy = 0; iy <= heightSegments; iy++) {
			List<Integer> verticesRow = new ArrayList<>();
			float v = (float) iy / heightSegments;

			for (int ix = 0; ix <= widthSegments; ix++) {
				float u = (float) ix / widthSegments;

				// vertex
				Vector3f vertex = new Vector3f();

				vertex.x = -radius * (float) Math.cos(phiStart + u * phiLength)
						* (float) Math.sin(thetaStart + v * thetaLength);
				vertex.y = radius * (float) Math.cos(thetaStart + v * thetaLength);
				vertex.z = radius * (float) Math.sin(phiStart + u * phiLength)
						* (float) Math.sin(thetaStart + v * thetaLength);

				vertexList.add(vertex);

				// normal
				Vector3f normal = new Vector3f();

				normal.x = vertex.x;
				normal.y = vertex.y;
				normal.z = vertex.z;

				normal.normalize();

				normalList.add(normal);

				// uv
				Vector2f uv = new Vector2f();

				uv.x = u;
				uv.y = 1f - v;

				uvList.add(uv);

				verticesRow.add(index++);
			}
			grid.add(verticesRow);
		}

		// indices
		for (int iy = 0; iy < heightSegments; iy++) {
			for (int ix = 0; ix < widthSegments; ix++) {
				int a = grid.get(iy).get(ix + 1);
				int b = grid.get(iy).get(ix);
				int c = grid.get(iy + 1).get(ix);
				int d = grid.get(iy + 1).get(ix + 1);

				if (iy != 0 || thetaStart > 0f) {
					if (ccw) {
						indexList.add(a);
						indexList.add(b);
						indexList.add(d);
					} else {
						indexList.add(d);
						indexList.add(b);
						indexList.add(a);
					}
				}

				if (iy != heightSegments - 1 || thetaEnd < Math.PI) {
					if (ccw) {
						indexList.add(b);
						indexList.add(c);
						indexList.add(d);
					} else {
						indexList.add(d);
						indexList.add(c);
						indexList.add(b);
					}
				}
			}
		}

		float[] positions = new float[vertexList.size() * 3];
		for (int i = 0; i < vertexList.size(); i++) {
			Vector3f vertex = vertexList.get(i);
			positions[i * 3 + 0] = vertex.x;
			positions[i * 3 + 1] = vertex.y;
			positions[i * 3 + 2] = vertex.z;
		}

		float[] texCoords = new float[uvList.size() * 2];
		for (int i = 0; i < uvList.size(); i++) {
			Vector2f uv = uvList.get(i);
			texCoords[i * 2 + 0] = uv.x;
			texCoords[i * 2 + 1] = uv.y;
		}

		float[] normals = new float[normalList.size() * 3];
		for (int i = 0; i < normalList.size(); i++) {
			Vector3f normal = normalList.get(i);
			normals[i * 3 + 0] = normal.x;
			normals[i * 3 + 1] = normal.y;
			normals[i * 3 + 2] = normal.z;
		}

		int[] indices = indexList.stream().mapToInt(i -> i).toArray();

		Mesh mesh = new Mesh(positions, texCoords, normals, indices);
		return mesh;
	}
}
