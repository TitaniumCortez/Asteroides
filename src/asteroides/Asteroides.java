/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asteroides;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author RCotez
 */
public class Asteroides extends SimpleApplication {

    /**
     * @param args the command line arguments
     */
    private boolean left = false, right = false, up = false, down = false;

    public static void main(String[] args) {
        // TODO code application logic here        
        Asteroides app = new Asteroides();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(120);
        settings.setTitle("Asteroides");
        app.setSettings(settings);
        app.start();
    }
    private Spatial player;
    private Geometry geometry_shots;
    private Node shots;
    private Node stones;
    private AudioNode laser, efeitoSonoro;
    private boolean stop_game = false;

    int MAX_MOUNT_STONE = 40;
    int MOUNT_STONE = 0;
    Spatial ninja;

    @Override
    public void simpleInitApp() {
        cameraSetting();// Configuraçoes da camera. 
        Box box1 = new Box(1, 1, 1);
        player = assetManager.loadModel("Wraith Raider Starship.obj");
        player.setLocalTranslation(new Vector3f(0, -45, 0));
        Material mat1 = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Red);
        player.scale(0.01f, 0.01f, 0.01f);
        //ninja.rotate(2f, 0f, 0.0f);
        player.lookAt(Vector3f.UNIT_Y, Vector3f.ZERO);
        player.setMaterial(mat1);
        Node pivot = new Node("pivot");
        creatScenery();
        setUpKeys();
        initAudio();
        speed = 15; // speed player 
        pivot.attachChild(player);
        stones = new Node();
        shots = new Node();
        rootNode.attachChild(pivot);
        creatStone();

        rootNode.attachChild(stones);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

    }
    private Ray ray = new Ray();
    private Ray shot_colissio = new Ray();

    public void simpleUpdate(float tpf) {
        //TODO: add update code

        if (stop_game != true) {
            Iterator itr = stones.getChildren().iterator();
            Iterator shot_colision = shots.getChildren().iterator();
            if (MOUNT_STONE != 0) {
                while (itr.hasNext()) {
                    Spatial stone = (Spatial) itr.next();
                    stone.rotate(0, 0, 0.09f * tpf);
                    Vector3f Vector_gravity = stone.getLocalTranslation();
                    stone.setLocalTranslation(Vector_gravity.x, (Vector_gravity.y - 0.015f), Vector_gravity.z);
                    if (Vector_gravity.y < -70) {
                        stone.removeFromParent();
                        System.out.println("stone destriudo da cena");
                        MOUNT_STONE--;
                    }

                }

            }

            // 1. Reset results list.
            CollisionResults results = new CollisionResults();
            // 2. Aim the ray from player location in player direction.
            ray.setOrigin(player.getLocalTranslation());
            //ray.setDirection(player.getLocalTranslation());
            // 3. Collect intersections between ray and all nodes in results list.
            rootNode.collideWith(ray, results);
            // 4. Use the result
            if (results.size() > 0) {
                // The closest result is the target that the player picked:
                Geometry target = results.getClosestCollision().getGeometry();
                // if camera closer than 10...

                if (player.getLocalTranslation().distance(target.getLocalTranslation()) < 10) {
                    String colission = target.getName();
                    // ... move the cube in the direction that camera is facing
                    // target.move(cam.getDirection());
                    if (colission.equals("stone")) {
                        player.removeFromParent();
                        BitmapText hudText = new BitmapText(guiFont, false);
                        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
                        hudText.setColor(ColorRGBA.Blue);                             // font color
                        hudText.setText("PERDEU");                                  // the text
                        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0); // position
                        guiNode.attachChild(hudText);
                        stop_game = true;
                    }

                }
            }

            Iterator shot_ = shots.getChildren().iterator();
            while (shot_.hasNext()) {
                Geometry shot = (Geometry) shot_.next();
                Vector3f Vector_gravityShot = shot.getLocalTranslation();
                shot.setLocalTranslation(Vector_gravityShot.x, (Vector_gravityShot.y + 0.095f), Vector_gravityShot.z);

                CollisionResults results_shot = new CollisionResults();
                shot_colissio.setOrigin(shot.getLocalTranslation());
                stones.collideWith(shot_colissio, results_shot);

                if (results_shot.size() > 0) {
                    Geometry target = results_shot.getClosestCollision().getGeometry();
                    if (shot.getLocalTranslation().distance(target.getLocalTranslation()) < 10) {
                        String colission = target.getName();
                        System.out.println(colission);
                        if (colission.equals("stone")) {
                            shot.removeFromParent();
                            target.removeFromParent();
                            MOUNT_STONE--;
                        }
                    }
                }
                if (Vector_gravityShot.y > 70) {
                    shot.removeFromParent();
                    System.out.println("Tiro destruido da cena");
                }
            }

            if (MOUNT_STONE == 0) {
                BitmapText hudText = new BitmapText(guiFont, false);
                        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
                        hudText.setColor(ColorRGBA.Blue);                             // font color
                        hudText.setText("Ganhou");                                  // the text
                        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0); // position
                        guiNode.attachChild(hudText);
                        stop_game = true;
            }
        }
    }

    private void initAudio() {
        /* gun shot sound is to be triggered by a mouse click. */
        laser = new AudioNode(assetManager, "laser.wav", DataType.Buffer);
        laser.setPositional(false);
        laser.setLooping(false);
        laser.setVolume(2);
        rootNode.attachChild(laser);

        efeitoSonoro = new AudioNode(assetManager, "efeitoSonoro.wav", DataType.Stream);
        efeitoSonoro.setLooping(true);  // activate continuous playing
        efeitoSonoro.setPositional(true);
        efeitoSonoro.setVolume(1);
        rootNode.attachChild(efeitoSonoro);
        efeitoSonoro.play(); // play continuously!
    }
    private boolean tiro = false;

    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Shot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(analogListener, "Left");
        inputManager.addListener(analogListener, "Right");
        inputManager.addListener(analogListener, "Up");
        inputManager.addListener(analogListener, "Down");
        inputManager.addListener(analogListener, "Shot");
    }
//Ação dos botao
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            Vector3f v = player.getLocalTranslation();
            if (name.equals("Left")) {
                if (v.x >= -70) {
                    player.setLocalTranslation(v.x - value * speed, v.y, v.z);
                }
            } else if ("Up".equals(name)) {
                if (v.y <= 50) {
                    player.setLocalTranslation(v.x, v.y + value * speed, v.z);

                }
            }
            if (name.equals("Right")) {
                if (v.x <= 70) {
                    player.setLocalTranslation(v.x + value * speed, v.y, v.z);

                }
            } else if (name.equals("Down")) {
                if (v.y >= -50) {
                    player.setLocalTranslation(v.x, v.y - value * speed, v.z);

                }
            }
            if (name.equals("Shot")) {
                if (tiro != true) {
                    Box shot = new Box(0.5f, 0.7f, 0.5f);
                    geometry_shots = new Geometry("Shot", shot);
                    geometry_shots.setLocalTranslation(player.getLocalTranslation());
                    Material mat1 = new Material(assetManager,
                            "Common/MatDefs/Misc/Unshaded.j3md");
                    mat1.setColor("Color", ColorRGBA.Green);
                    geometry_shots.setMaterial(mat1);
                    shots.attachChild(geometry_shots);
                    laser.playInstance();
                    rootNode.attachChild(shots);
                    tiro = true;
                } else {
                    // delay pra o proximo tiro  

                    try {
                        getBusyFlag();
                        tiro = false;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Asteroides.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }
    };

    float frustumSize = 55;

    public void cameraSetting() {
        //Muda o modo de projeção
        cam.setParallelProjection(true);
        //Calcula a proporção entre altura e largura
        float aspect = (float) cam.getWidth() / cam.getHeight();
        //Atualiza os limites de renderização de acordo com o calculado
        cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
        //Atualiza a câmera
        cam.update();
        setDisplayStatView(false);
        flyCam.setEnabled(false);
    }

    int l = 70;
    int a = 50;

    //Cria o cenario 
    public void creatScenery() {
        Node start = new Node();
        Random randoLargura = new Random();
        Random randoComprimento = new Random();

        for (int i = 0; i <= 10; i++) {
            Sphere sphereMesh = new Sphere(10, 10, -0.5f);
            Geometry sphereGeo = new Geometry("Start", sphereMesh);
            Material sphereMat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            sphereMat.setColor("Color", ColorRGBA.Gray);
            sphereGeo.setMaterial(sphereMat);
            sphereGeo.setLocalTranslation(randoLargura.nextInt(l), randoComprimento.nextInt(a), 0);
            start.attachChild(sphereGeo);

        }
        rootNode.attachChild(start);
        for (int i = 0; i <= 10; i++) {
            Sphere sphereMesh = new Sphere(10, 10, -0.5f);
            Geometry sphereGeo = new Geometry("Start", sphereMesh);
            Material sphereMat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            sphereMat.setColor("Color", ColorRGBA.Gray);
            sphereGeo.setMaterial(sphereMat);
            sphereGeo.setLocalTranslation(-randoLargura.nextInt(l), -randoComprimento.nextInt(a), 0);
            start.attachChild(sphereGeo);

        }

        rootNode.attachChild(start);

        for (int i = 0; i <= 10; i++) {
            Sphere sphereMesh = new Sphere(10, 10, -0.5f);
            Geometry sphereGeo = new Geometry("Start", sphereMesh);
            Material sphereMat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            sphereMat.setColor("Color", ColorRGBA.Gray);
            sphereGeo.setMaterial(sphereMat);
            sphereGeo.setLocalTranslation(randoLargura.nextInt(l), -randoComprimento.nextInt(a), 0);
            start.attachChild(sphereGeo);

        }
        rootNode.attachChild(start);
        for (int i = 0; i <= 10; i++) {
            Sphere sphereMesh = new Sphere(10, 10, -0.5f);
            Geometry sphereGeo = new Geometry("Start", sphereMesh);
            Material sphereMat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            sphereMat.setColor("Color", ColorRGBA.Gray);
            sphereGeo.setMaterial(sphereMat);
            sphereGeo.setLocalTranslation(-randoLargura.nextInt(l), randoComprimento.nextInt(a), 0);
            start.attachChild(sphereGeo);

        }
        rootNode.attachChild(start);
        Sphere sphereMesh = new Sphere(32, 32, 2f);
        Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
        Material sphereMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        sphereMat.setColor("Diffuse", ColorRGBA.Gray);
        sphereMat.setColor("Specular", ColorRGBA.Gray);
        sphereMat.setFloat("Shininess", 64f);  // [0,128]
        sphereGeo.setMaterial(sphereMat);
        sphereGeo.setLocalTranslation(-40, 20, 0);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        rootNode.attachChild(sphereGeo);
    }

    public void creatStone() {
        Random random_stone = new Random();
        while (MOUNT_STONE < MAX_MOUNT_STONE) {
            int x = -70 + (int) (Math.random() * ((70 - (-70)) + 1));
            int y = 55 + (int) (Math.random() * 60);
            Box box1 = new Box(1, 1, 1);
            Geometry stone = new Geometry("stone", box1);
            stone.setLocalTranslation(new Vector3f(x, y, 0));
            Material mat1 = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", ColorRGBA.Blue);
            System.out.println(x + "---" + y);
            stone.setMaterial(mat1);
            stones.attachChild(stone);
            MOUNT_STONE++;
        }
    }

    public synchronized void getBusyFlag() throws InterruptedException {
        wait(100);
    }

}
