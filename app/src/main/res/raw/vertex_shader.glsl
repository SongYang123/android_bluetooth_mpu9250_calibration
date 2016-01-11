uniform mat4 uMVPMatrix;
attribute vec4 aPosition;

void main() {
	gl_PointSize = 3.0;
	gl_Position = uMVPMatrix * aPosition;
}
