package br.com.lbbento.main;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

import br.com.lbbento.glapp.*;
import br.com.lbbento.glapp.model.*;

/*
 * @author lbbento
 * 
 * Trabalho de Computação Gráfica
 * Exemplo de uso de duas cameras, usando a bibliotec lwjgl com a implementação do glapp
 * 
 * 
 */

public class Main extends GLApp {


	//Início implementação controle da ave
	private int birdRotationX = 0; 	
	    
    // Posição inicial da camera
    float[] cameraPos = {0f,3f,20f};

    // Iluminação
    float lightPosition[]= { -2f, 2f, 2f, 0f };


    // Cria duas cameras usando o objeto GLCamera - que implementa o gluLookAt
    GLCamera camera1 = new GLCamera();
    GLCamera camera2 = new GLCamera();
    GLCam cam = new GLCam(camera1); //GLCam é responśavel por tratar os atalhos de teclado e girar camera. - INicia com a camera 1
    
    
    // Texturas da bola e da grama
    int ballTextureHandle = 0;
    int grassTextureHandle = 0;

    // BOla de futebol com textura 
    int ball;
    
    
    

    // modelo do passáro 
    GLModel bird;
    //Movimentação
    GL_Vector UP = new GL_Vector(0,1,0);
    GL_Vector ORIGIN = new GL_Vector(0,0,0);
    // sombra
    GLShadowOnPlane birdShadow;
    //Posição
    public GL_Vector birdPos;
    //animação - fica girando
    float birdAnimationDegrees = 0;
    
	
    
    FloatBuffer bbmatrix = GLApp.allocFloats(16);

    /**
     * Abre a aplicação e controla interação com o usuário
     */
    public static void main(String args[]) {
    	Main app3d = new Main();
    	
    	//configure tela
    	app3d.VSyncEnabled = true;
    	app3d.fullScreen = false;
    	app3d.displayWidth = 800;
    	app3d.displayHeight = 600;
    	app3d.window_title = "Lucas Bento - Trabalho 3d";
    	app3d.run();  // wAqui ele chama o init(), o render() pra desenhar e controla os atalhos
    }

    
    /**
     * Método sobrescrito, chamado pelo Glapp. Configura ambiente.
     */
    @Override
    public void setup()
    {
        // Configura campo de visão
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(50f,         // zoom
                           aspectRatio, // shape of viewport rectangle
                           .1f,         // Minimo Z
                           500f);       // maximo Z
        // returna para modelo de matriz
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
        
        

        //FUnção glapp para criar luz 
        //tipo da luz, e posição da mesma
        setLight( GL11.GL_LIGHT1,
        		new float[] { 1f, 1f, 1f, 1f },
        		new float[] { 0.5f, 0.5f, .53f, 1f },
        		new float[] { 1f, 1f, 1f, 1f },
        		lightPosition );

        // Isso server para refletir a grama
        setLight( GL11.GL_LIGHT2,
        		new float[] { 0.15f, 0.4f, 0.1f, 1.0f },  
        		new float[] { 0.0f, 0.0f, 0.0f, 1.0f },   
        		new float[] { 0.0f, 0.0f, 0.0f, 1.0f },   
        		new float[] { 0.0f, -10f, 0.0f, 0f } );   
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        //Inicializa texturas
        ballTextureHandle = makeTexture("images/bola.gif");
        grassTextureHandle = makeTexture("images/grama.jpg",true,true);

        
        
        // Posiciona camera 1
        camera1.setCamera(0,4,15, 0,-.3f,-1, 0,1,0);

        // Carrega o pássaro - modelo do 3d studio scene
        bird = new GLModel("models/eagle/EAGLE_2.OBJ");
        //bird = new GLModel("models/dragon/Gringotts Dragon.obj");
        bird.mesh.regenerateNormals();
        bird.makeDisplayList();

        // Cria uma esfera normal e carrega um textura de bola de futebol.
        ball = beginDisplayList(); 
       	renderSphere();
        endDisplayList();

        //Cria um controlador de sombra - muito interessante, passa a posição da luz, 
        //onde vai ser desenhada a sombra, os atributos da sombra... e ele controla dependendo da movimentação do MOdel
        birdShadow = new GLShadowOnPlane(lightPosition, new float[] {0f,1f,0f,3f}, null, this, method(this,"drawObjects"));
    }



    /**
     * Renderiza objetos(desenha), chamado pela app principal do glapp
     */
    @Override
    public void draw() {
    	
    	//Faz animação automática..
    	// TODO  Se der tempo, implementar controle manual para os objetos - LUCAS
    	birdAnimationDegrees += 30f * GLApp.getSecondsPerFrame();

        // o pássaro fica ao redor da bola, e a camera 2, acompanha o pássaro
    	birdPos = GL_Vector.rotationVector(birdAnimationDegrees).mult(8);
    	//camera2.MoveTo(birdPos.x, birdPos.y+.2f, birdPos.z);
    	camera2.MoveTo(birdPos.x, birdPos.y+.53f, birdPos.z);

    	// usando a função crossProduct para achar a inclinação da caemra, referente ao movimento do pássaro, assim a camera vai olhar sempre
    	//para frente
        GL_Vector airplaneDirection = GL_Vector.crossProduct(UP,birdPos);
    	camera2.viewDir( airplaneDirection ); //atualiza direção da cam
        float apRot = camera2.RotatedY;  
    	camera2.RotatedY = 0;            
    	camera2.RotateY(apRot);         

        // usa o poder do lwjgl quanto aos comandos e integração com a camera. 
        cam.handleNavKeys((float)GLApp.getSecondsPerFrame());


        // limpa buffer 
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // usa o gluLookAt para atualizar a camera caso ela tenha sido deslocada para algum lado.
        cam.render();

        
        
        // Desenhando objetos - começando pela grama
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(0f, -3f, 0f); // down a bit
            GL11.glScalef(15f, .01f, 15f);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, grassTextureHandle); //liga textura - normal
            renderCube();
        }
        GL11.glPopMatrix();

        //sombra
        birdShadow.drawShadow();

        //rotação da bola, aumentar para rodar mais rápida
        rotation += 150f * getSecondsPerFrame();

        // desenha objetos, tem q ser depois que desenha sombra... pelo menos assim deu certo...
        drawObjects();

        // Cria a iluminação.. com a posição pré definida
        setLightPosition(GL11.GL_LIGHT1, lightPosition);


        
        // Escreve comandos na tela - depois, acrescentar comandos do pássaro
		print( 30, viewportH- 45, "Use as seguintes teclas controlar:");
        print( 30, viewportH- 80, "Esquerda e Direita: giram a camera.", 1);
        print( 30, viewportH-100, "Baixo e Cima: move a camera para frente e para tras.", 1);
        print( 30, viewportH-120, "PageUp e PageDown: move a camera na vertical.", 1);
        print( 30, viewportH-140, "Espaco: troca de camera! Camera Principal ou do Passaro.", 1);
        print( 30, viewportH-160, "", 1);
        print( 30, viewportH-180, "Por enquanto somente animacao. Estao sendo criados os controles", 1);
        print( 30, viewportH-192, "para interagir com o pássaro.", 1);
    }

    public void drawObjects() {
        // Desenha o pássaro
        GL11.glPushMatrix();
        {
        	//animação - girabdo em torno da bola
        	billboardPoint(birdPos, ORIGIN, UP); //movimenta
            GL11.glRotatef(-90, 0, 1, 0); //vira pássaro de frente
            
            //controle ave - tecla 1 e 2 - inclinação
            GL11.glRotatef(birdRotationX, 1, 0, 0);
            
            
            GL11.glScalef(4f, 4f, 4f);
        	bird.render();
        	
        	//reset
            setMaterial( new float[] {.8f, .8f, .7f, 1f}, .4f);
        }
        GL11.glPopMatrix();

    	// Desenha a bola de futebol
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(0f, -1f, 0f); // posiciona bola bem na grama
            
            GL11.glRotatef(rotation, 0, 1, 0);  
            GL11.glScalef(2f, 2f, 2f);          
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ballTextureHandle);
            callDisplayList(ball);
        }
        GL11.glPopMatrix();
    }

	/**
	 * Cria uma matriz com as faces à ser desenhadas
	 */
	public void billboardPoint(GL_Vector bbPos, GL_Vector targetPos, GL_Vector targetUp)
	{
		GL_Vector look = GL_Vector.sub(targetPos,bbPos).normalize();
		GL_Vector right = GL_Vector.crossProduct(targetUp,look).normalize();
		GL_Vector up = GL_Vector.crossProduct(look,right).normalize();
		GL_Matrix.createBillboardMatrix(bbmatrix, right, up, look, bbPos);
		GL11.glMultMatrix(bbmatrix);
	}
	
    public void mouseMove(int x, int y) {
    }

    public void mouseDown(int x, int y) {
    }

    public void mouseUp(int x, int y) {
    }

    public void keyDown(int keycode) {
    	
    	//troca de camêra... tem duas,, a geral e a do pássaro
    	if (Keyboard.KEY_SPACE == keycode) {
    		cam.setCamera((cam.camera == camera1)? camera2 : camera1);
    		
    	//controle da ave - implementando	
    	}else if (Keyboard.KEY_1 == keycode) {
    		birdRotationX += 10;
       	}else if (Keyboard.KEY_2 == keycode) {
    		birdRotationX -= 10;
       	}

    }

    public void keyUp(int keycode) {
    }
    
}