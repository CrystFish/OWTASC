
public class Vector2
{
  float x;
  float y;
  
  Vector2(){
    x = y = 0;
  }

  Vector2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  Vector2(Vector2 vec)
  {
    this.assign(vec);
  }

  void assign(Vector2 vec)
  {
    x = vec.x;
    y = vec.y;
  }
  
  String toString(){
    return "(" + x + ", " + y + ")";
  }
  
  float dist(Vector2 v){
    return v.sub(this).magnitude();
  }
  
  Vector2 add(Vector2 v){
    return new Vector2(this.x + v.x, this.y + v.y);
  }
  
  Vector2 sub(Vector2 v){
    return new Vector2(this.x - v.x, this.y - v.y);
  }
  
  Vector2 mult(float value){
    return new Vector2(this.x * value, this.y * value);
  }
  
  Vector2 scale(float value){
    this.x *= value;
    this.y *= value;
    return this;
  }
  
  float magnitude(){
    return max(sqrt(x*x+y*y), 0.001f);
  }
  
  Vector2 normalize(){
    float mag = magnitude();
    x /= mag;
    y /= mag;
    return this;
  }
  
  Vector2 normalized(){
    float mag = magnitude();
    return new Vector2(x/mag, y/mag);
  }
  
  float theta(){
    return (float)Math.atan2(y, x);
  }
  
  float dx(){
    return x/magnitude();
  }
  
  float dy(){
    return y/magnitude();
  }
  
  Vector2 leftNormal(){
    return new Vector2(y,-x);
  }
  Vector2 rightNormal(){
    return new Vector2(-y,x);
  }
  
  float dot(Vector2 v1){
    return(this.x * v1.x + this.y * v1.y);
  }
  
  float scaledDot(Vector2 v1){
    return(this.x * v1.dx() + this.y * v1.dy());
  }
  
  Vector2 projectOnto(Vector2 v2){
    float dot = this.scaledDot(v2);
    return new Vector2(dot * v2.dx(), dot * v2.dy());
  }
}