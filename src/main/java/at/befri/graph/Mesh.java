package at.befri.graph;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import at.befri.engine.items.GameItem;
import at.befri.graph.shadow.ShadowRenderer;

public class Mesh {
	private final int vaoId;
	private final List<Integer> vboIdList;
	private final int vertexCount;
	private IMaterial iMaterial;

	public Mesh(float[] positions, float[] texCoords, float[] normals, int[] indices) {
		FloatBuffer posBuffer = null;
		FloatBuffer texCoordsBuffer = null;
		FloatBuffer vecNormalsBuffer = null;
		FloatBuffer vecTangentsBuffer = null;
		FloatBuffer vecBitangentsBuffer = null;
		IntBuffer indicesBuffer = null;
		try {
			iMaterial = new Material();
			vertexCount = indices.length;
			vboIdList = new ArrayList<>();

			// Create the VAO and bind to it
			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			// Position VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // Define structure of the data

			// Texture coordinates VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
			texCoordsBuffer.put(texCoords).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

			// Vertex normals VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
			vecNormalsBuffer.put(normals).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

			float[] tangents = new float[normals.length];
			float[] bitangents = new float[normals.length];

			computeTangentBasis(positions, texCoords, normals, indices, tangents, bitangents);

			// Tangents VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			vecTangentsBuffer = MemoryUtil.memAllocFloat(tangents.length);
			vecTangentsBuffer.put(tangents).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecTangentsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

			// Bitangents VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			vecBitangentsBuffer = MemoryUtil.memAllocFloat(bitangents.length);
			vecBitangentsBuffer.put(bitangents).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecBitangentsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			// Unbind the VBO
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			// Unbind the VAO
			glBindVertexArray(0);
		} finally {
			if (posBuffer != null) {
				MemoryUtil.memFree(posBuffer);
			}
			if (texCoordsBuffer != null) {
				MemoryUtil.memFree(texCoordsBuffer);
			}
			if (vecNormalsBuffer != null) {
				MemoryUtil.memFree(vecNormalsBuffer);
			}
			if (indicesBuffer != null) {
				MemoryUtil.memFree(indicesBuffer);
			}
		}
	}

	private static void computeTangentBasis(float[] positions, float[] texCoords, float[] normals, int[] indices,
			float[] tangents, float[] bitangents) {
//		 for (int i = 0; i < normals.length; i++) {
//		 tangents[i] = 0.0f;
//		 bitangents[i] = 0.0f;
//		 }
//		
//		 for (int i = 0; i < indices.length; i += 3) {
//		 int i0 = indices[i + 0];
//		 int i1 = indices[i + 1];
//		 int i2 = indices[i + 2];
//		
//		 Vector3f v0 = new Vector3f(positions[i0 * 3 + 0], positions[i0 * 3 + 1], positions[i0 * 3
//		 + 2]);
//		 Vector3f v1 = new Vector3f(positions[i1 * 3 + 0], positions[i1 * 3 + 1], positions[i1 * 3
//		 + 2]);
//		 Vector3f v2 = new Vector3f(positions[i2 * 3 + 0], positions[i2 * 3 + 1], positions[i2 * 3
//		 + 2]);
//		
//		 Vector2f uv0 = new Vector2f(texCoords[i0 * 2 + 0], texCoords[i0 * 2 + 1]);
//		 Vector2f uv1 = new Vector2f(texCoords[i1 * 2 + 0], texCoords[i1 * 2 + 1]);
//		 Vector2f uv2 = new Vector2f(texCoords[i2 * 2 + 0], texCoords[i2 * 2 + 1]);
//		
//		 float x0 = v1.x - v0.x;
//		 float x1 = v2.x - v0.x;
//		 float y0 = v1.y - v0.y;
//		 float y1 = v2.y - v0.y;
//		 float z0 = v1.z - v0.z;
//		 float z1 = v2.z - v0.z;
//		
//		 float s0 = uv1.x - uv0.x;
//		 float s1 = uv2.x - uv0.x;
//		 float t0 = uv1.y - uv0.y;
//		 float t1 = uv2.y - uv0.y;
//		
//		 float aux = (s0 * t1 - s1 * t0);
//		 float r;
//		 if (aux != 0.0f) {
//		 r = 1.0f / aux;
//		 } else {
//		 r = 1.0f;
//		 }
//		
//		 Vector3f sdir = new Vector3f((t1 * x0 - t0 * x1) * r, (t1 * y0 - t0 * y1) *
//		 r, (t1 * z0 - t0 * z1) * r);
//		 Vector3f tdir = new Vector3f((s0 * x1 - s1 * x0) * r, (s0 * y1 - s1 * y0) *
//		 r, (s0 * z1 - s1 * z0) * r);
//		
//		 tangents[i0 * 3 + 0] += sdir.x;
//		 tangents[i0 * 3 + 1] += sdir.y;
//		 tangents[i0 * 3 + 2] += sdir.z;
//		 tangents[i1 * 3 + 0] += sdir.x;
//		 tangents[i1 * 3 + 1] += sdir.y;
//		 tangents[i1 * 3 + 2] += sdir.z;
//		 tangents[i2 * 3 + 0] += sdir.x;
//		 tangents[i2 * 3 + 1] += sdir.y;
//		 tangents[i2 * 3 + 2] += sdir.z;
//		
//		 bitangents[i0 * 3 + 0] += tdir.x;
//		 bitangents[i0 * 3 + 1] += tdir.y;
//		 bitangents[i0 * 3 + 2] += tdir.z;
//		 bitangents[i1 * 3 + 0] += tdir.x;
//		 bitangents[i1 * 3+ 1] += tdir.y;
//		 bitangents[i1 * 3+ 2] += tdir.z;
//		 bitangents[i2 * 3+ 0] += tdir.x;
//		 bitangents[i2 * 3+ 1] += tdir.y;
//		 bitangents[i2 * 3+ 2] += tdir.z;
//		 }

		// for (long a = 0; a < vertexCount; a++)
		// {
		// const Vector3D& n = normal[a];
		// const Vector3D& t = tan1[a];
		//
		// // Gram-Schmidt orthogonalize
		// tangent[a] = (t - n * Dot(n, t)).Normalize();
		//
		// // Calculate handedness
		// tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;
		// }
		//
		// delete[] tan1;
		// }

		// for(int i = 0; i < indices.length; i += 3) {
		// int i0 = indices[i + 0];
		// int i1 = indices[i + 1];
		// int i2 = indices[i + 2];
		//
		// Vector3f vertex0 = new Vector3f(positions[i0 + 0], positions[i0 + 1],
		// positions[i0 + 2]);
		// Vector3f vertex1 = new Vector3f(positions[i1 + 0], positions[i1 + 1],
		// positions[i1 + 2]);
		// Vector3f vertex2 = new Vector3f(positions[i2 + 0], positions[i2 + 1],
		// positions[i2 + 2]);
		//
		// Vector3f a = new Vector3f();
		// vertex1.sub(vertex0, a);
		// Vector3f b = new Vector3f();
		// vertex2.sub(vertex0, b);
		// Vector3f normal = new Vector3f();
		// a.cross(b, normal);
		//
		// Vector3f deltaPos = vertex0.equals(vertex1) ? b : a;
		//
		// Vector2f uv0 = new Vector2f(texCoords[i0 + 0], texCoords[i0 + 1]);
		// Vector2f uv1 = new Vector2f(texCoords[i1 + 0], texCoords[i1 + 1]);
		// Vector2f uv2 = new Vector2f(texCoords[i2 + 0], texCoords[i2 + 1]);
		//
		// Vector2f deltaUV1 = new Vector2f();
		// uv1.sub(uv0, deltaUV1);
		// Vector2f deltaUV2 = new Vector2f();
		// uv2.sub(uv0, deltaUV2);
		//
		// Vector3f tan = new Vector3f(); // tangents
		// Vector3f bin = new Vector3f(); // binormal
		//
		// // avoid divion with 0
		// if(deltaUV1.x != 0.0f) {
		// deltaPos.div(deltaUV1.x, tan);
		// } else {
		// deltaPos.div(1.0f, tan);
		// }
		//
		// Vector3f c = new Vector3f();
		// normal.mul(normal.dot(tan), c);
		// tan.sub(c).normalize();
		//
		// tan.cross(normal, bin);
		// bin.normalize();
		//
		// // write into array - for each vertex of the face the same value
		// tangents[i0 + 0] = tan.x;
		// tangents[i0 + 1] = tan.y;
		// tangents[i0 + 2] = tan.z;
		//
		// tangents[i1 + 0] = tan.x;
		// tangents[i1 + 1] = tan.y;
		// tangents[i1 + 2] = tan.z;
		//
		// tangents[i2 + 0] = tan.x;
		// tangents[i2 + 1] = tan.y;
		// tangents[i2 + 2] = tan.z;
		//
		// binormals[i0 + 0] = bin.x;
		// binormals[i0 + 1] = bin.y;
		// binormals[i0 + 2] = bin.z;
		//
		// binormals[i1 + 0] = bin.x;
		// binormals[i1 + 1] = bin.y;
		// binormals[i1 + 2] = bin.z;
		//
		// binormals[i2 + 0] = bin.x;
		// binormals[i2 + 1] = bin.y;
		// binormals[i2 + 2] = bin.z;
		// }
//		List<Vector3f> vertexList = new ArrayList<>();
//		List<Vector2f> uvList = new ArrayList<>();
//		List<Vector3f> normalList = new ArrayList<>();
//		List<Vector3f> tangentList = new ArrayList<>();
//		List<Vector3f> bitangentList = new ArrayList<>();

		for (int i = 0; i < indices.length; i += 3) {
			int i0 = indices[i + 0];
			int i1 = indices[i + 1];
			int i2 = indices[i + 2];
			
			Vector3f v0 = new Vector3f(positions[i0 * 3 + 0], positions[i0 * 3 + 1], positions[i0 * 3 + 2]);
			Vector3f v1 = new Vector3f(positions[i1 * 3 + 0], positions[i1 * 3 + 1], positions[i1 * 3 + 2]);
			Vector3f v2 = new Vector3f(positions[i2 * 3 + 0], positions[i2 * 3 + 1], positions[i2 * 3 + 2]);
			
			Vector2f uv0 = new Vector2f(texCoords[i0 * 2 + 0], texCoords[i0 * 2 + 1]);
			Vector2f uv1 = new Vector2f(texCoords[i1 * 2 + 0], texCoords[i1 * 2 + 1]);
			Vector2f uv2 = new Vector2f(texCoords[i2 * 2 + 0], texCoords[i2 * 2 + 1]);
			
			Vector3f n0 = new Vector3f(normals[i0 * 3 + 0], normals[i0 * 3 + 1], normals[i0 * 3 + 2]);
			Vector3f n1 = new Vector3f(normals[i1 * 3 + 0], normals[i1 * 3 + 1], normals[i1 * 3 + 2]);
			Vector3f n2 = new Vector3f(normals[i2 * 3 + 0], normals[i2 * 3 + 1], normals[i2 * 3 + 2]);
			
			Vector3f t0 = new Vector3f();
			Vector3f t1 = new Vector3f();
			Vector3f t2 = new Vector3f();
			
			Vector3f b0 = new Vector3f();
			Vector3f b1 = new Vector3f();
			Vector3f b2 = new Vector3f();
			
			Vector3f deltaPos1 = new Vector3f();
			v1.sub(v0, deltaPos1);
			Vector3f deltaPos2 = new Vector3f();
			v2.sub(v0, deltaPos2);
			
			Vector2f deltaUV1 = new Vector2f();
			uv1.sub(uv0, deltaUV1);
			Vector2f deltaUV2 = new Vector2f();
			uv2.sub(uv0, deltaUV2);
			
			float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
			Vector3f a = new Vector3f();
			deltaPos1.mul(deltaUV2.y, a);
			Vector3f b = new Vector3f();
			deltaPos2.mul(deltaUV1.y, b);
			Vector3f c = new Vector3f();
			a.sub(b, c);
			Vector3f tangent = new Vector3f();
			c.mul(r, tangent);
			Vector3f d = new Vector3f();
			deltaPos2.mul(deltaUV1.x, d);
			Vector3f e = new Vector3f();
			deltaPos1.mul(deltaUV2.x, e);
			Vector3f f = new Vector3f();
			d.sub(e, f);
			Vector3f bitangent = new Vector3f();
			f.mul(r, bitangent);
			
			t0 = magic(n0, tangent, bitangent);
			t1 = magic(n1, tangent, bitangent);
			t2 = magic(n2, tangent, bitangent);
			
			tangents[i0 * 3 + 0] = t0.x;
			tangents[i0 * 3 + 1] = t0.y;
			tangents[i0 * 3 + 2] = t0.z;
			tangents[i1 * 3 + 0] = t1.x;
			tangents[i1 * 3 + 1] = t1.y;
			tangents[i1 * 3 + 2] = t1.z;
			tangents[i2 * 3 + 0] = t2.x;
			tangents[i2 * 3 + 1] = t2.y;
			tangents[i2 * 3 + 2] = t2.z;
			
			n0.cross(t0, b0); 
			n1.cross(t1, b1);
			n2.cross(t2, b2);
			
			bitangents[i0 * 3 + 0] = bitangent.x;
			bitangents[i0 * 3 + 1] = bitangent.y;
			bitangents[i0 * 3 + 2] = bitangent.z;
			bitangents[i1 * 3 + 0] = bitangent.x;
			bitangents[i1 * 3 + 1] = bitangent.y;
			bitangents[i1 * 3 + 2] = bitangent.z;
			bitangents[i2 * 3 + 0] = bitangent.x;
			bitangents[i2 * 3 + 1] = bitangent.y;
			bitangents[i2 * 3 + 2] = bitangent.z;
		}

//		for (int i = 0; i < vertexList.size(); i += 3) {
//			// Shortcuts for vertices
//			Vector3f v0 = vertexList.get(i + 0);
//			Vector3f v1 = vertexList.get(i + 1);
//			Vector3f v2 = vertexList.get(i + 2);
//
//			// Shortcuts for UVs
//			Vector2f uv0 = uvList.get(i + 0);
//			Vector2f uv1 = uvList.get(i + 1);
//			Vector2f uv2 = uvList.get(i + 2);
//
//			// Edges of the triangle : position delta
//			Vector3f deltaPos1 = new Vector3f();
//			v1.sub(v0, deltaPos1);
//			Vector3f deltaPos2 = new Vector3f();
//			v2.sub(v0, deltaPos2);
//
//			// UV delta
//			Vector2f deltaUV1 = new Vector2f();
//			uv1.sub(uv0, deltaUV1);
//			Vector2f deltaUV2 = new Vector2f();
//			uv2.sub(uv0, deltaUV2);
//
//			float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
//			Vector3f a = new Vector3f();
//			deltaPos1.mul(deltaUV2.y, a);
//			Vector3f b = new Vector3f();
//			deltaPos2.mul(deltaUV1.y, b);
//			Vector3f c = new Vector3f();
//			a.sub(b, c);
//			Vector3f tangent = new Vector3f();
//			c.mul(r, tangent);
//			Vector3f d = new Vector3f();
//			deltaPos2.mul(deltaUV1.x, d);
//			Vector3f e = new Vector3f();
//			deltaPos1.mul(deltaUV2.x, e);
//			Vector3f f = new Vector3f();
//			d.sub(e, f);
//			Vector3f bitangent = new Vector3f();
//			f.mul(r, bitangent);
//
//			Vector3f n0 = normalList.get(i + 0);
//			Vector3f n1 = normalList.get(i + 1);
//			Vector3f n2 = normalList.get(i + 2);
//			
//			tangentList.add(magic(n0, tangent, bitangent));
//			tangentList.add(magic(n1, tangent, bitangent));
//			tangentList.add(magic(n2, tangent, bitangent));
//			
//			bitangentList.add(bitangent);
//			bitangentList.add(bitangent);
//			bitangentList.add(bitangent);
//		}

		
	}

	private static Vector3f magic(Vector3f n, Vector3f t, Vector3f b) {
		Vector3f result = new Vector3f();
		
		// Gram-Schmidt orthogonalize
		Vector3f a = new Vector3f();
		n.mul(n.dot(t), a);
		t.sub(a, result);
		result.normalize();

		// Calculate handedness
		a = new Vector3f();
		n.cross(result, a);
		if (a.dot(b) < 0.0f) {
			return result.mul(-1.0f);
		}
		return result;
	}

	public int getVaoId() {
		return vaoId;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public IMaterial getIMaterial() {
		return iMaterial;
	}

	public Mesh setIMaterial(IMaterial material) {
		this.iMaterial = material;
		return this;
	}

	public void initRender() {
		if (iMaterial.isMultilayered()) {
			MultilayeredMaterial multilayeredMaterial = (MultilayeredMaterial) iMaterial;
			Texture[] diffuseMaps = multilayeredMaterial.getDiffuseMaps();
			Texture[] normalMaps = multilayeredMaterial.getNormalMaps();
			for (int i = 0; i < MultilayeredMaterial.MAX_LAYERS; i++) {
				if (i < diffuseMaps.length) {
					Texture diffuseMap = diffuseMaps[i];
					if (diffuseMap != null) {
						// Activate texture bank
						glActiveTexture(GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES + i * 2 + 0);
						// Bind the texture
						glBindTexture(GL_TEXTURE_2D, diffuseMap.getId());
					}
				}
				if (i < normalMaps.length) {
					Texture normalMap = normalMaps[i];
					if (normalMap != null) {
						// Activate texture bank
						glActiveTexture(GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES + i * 2 + 1);
						// Bind the texture
						glBindTexture(GL_TEXTURE_2D, normalMap.getId());
					}
				}
			}

			if (multilayeredMaterial.hasRgbaMap()) {
				Texture rgbaMap = multilayeredMaterial.getRgbaMap();
				glActiveTexture(GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES + 10);
				glBindTexture(GL_TEXTURE_2D, rgbaMap.getId());
			}
		} else {
			Material material = (Material) iMaterial;
			Texture diffuseMap = material.getDiffuseMap();
			if (diffuseMap != null) {
				// Activate texture bank
				glActiveTexture(GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES + 0);
				// Bind the texture
				glBindTexture(GL_TEXTURE_2D, diffuseMap.getId());
			}
			Texture normalMap = material.getNormalMap();
			if (normalMap != null) {
				// Activate texture bank
				glActiveTexture(GL_TEXTURE0 + ShadowRenderer.NUM_CASCADES + 1);
				// Bind the texture
				glBindTexture(GL_TEXTURE_2D, normalMap.getId());
			}

		}
		// Bind the VAO
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glEnableVertexAttribArray(4);
	}

	public void endRender() {
		// Restore state
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glDisableVertexAttribArray(4);
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void render() {
		initRender();

		// Draw the mesh
		glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

		endRender();
	}

	public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {
		initRender();

		for (GameItem gameItem : gameItems) {
			// Set up date required by gameItem
			consumer.accept(gameItem);

			// Render this game item
			glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
		}

		endRender();
	}

	public void cleanUp() {
		glDisableVertexAttribArray(0);

		// Delete the VBOs
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		vboIdList.forEach(vboId -> glDeleteBuffers(vboId));

		// Delete the textures
		if (iMaterial.isMultilayered()) {
			MultilayeredMaterial multilayeredMaterial = (MultilayeredMaterial) iMaterial;
			Texture[] diffuseMaps = multilayeredMaterial.getDiffuseMaps();
			Texture[] normalMaps = multilayeredMaterial.getNormalMaps();
			for (int i = 0; i < MultilayeredMaterial.MAX_LAYERS; i++) {
				if (i < diffuseMaps.length) {
					Texture diffuseMap = diffuseMaps[i];
					if (diffuseMap != null) {
						diffuseMap.cleanup();
					}
				}
				if (i < normalMaps.length) {
					Texture normalMap = normalMaps[i];
					if (normalMap != null) {
						normalMap.cleanup();
					}
				}
			}

			if (multilayeredMaterial.hasRgbaMap()) {
				Texture rgbaMap = multilayeredMaterial.getRgbaMap();
				rgbaMap.cleanup();
			}
		} else {
			Material material = (Material) iMaterial;
			Texture diffuseMap = material.getDiffuseMap();
			if (diffuseMap != null) {
				diffuseMap.cleanup();
			}
			Texture normalMap = material.getNormalMap();
			if (normalMap != null) {
				normalMap.cleanup();
			}
		}

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
}
