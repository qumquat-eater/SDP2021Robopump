<<<<<<< HEAD
"""joint_rotator_test controller."""
=======
>>>>>>> 9c593a7e1e9e205e2acb7b44c78db8ad92ee9a62
from controller import Robot, DistanceSensor, TouchSensor, Motor
from socket import *
import time
import math
import sympy as sym
import numpy as np
import cv2
from trans_matrix import FKWB
from detection import detect_fuel_cap
<<<<<<< HEAD
=======
#length of link from joint 2 (R2) to joint 3 (R3)
LENGTH1 = 0.189999
#length of link from joint 3 (R3) to joint 4 (R4)
LENGTH2 = 0.139

def getLinkLengths():
    return LENGTH1, LENGTH2

robot = Robot()
distanceSensor = robot.getDevice('ds1')
distanceSensor.enable(1)

class RoboPump:
    def __init__(self):
        # MOTORS
        self.baseMotor = robot.getDevice('base')  # min:-90, max:90
        self.upperarmMotor = robot.getDevice('upperarm')  # min:-90, max:90
        self.forearmMotor = robot.getDevice('forearm')  # min:-90, max:90
        self.wristMotor = robot.getDevice('wrist')  # min:-90, max:90
        self.rotationalwristMotor = robot.getDevice('rotational_wrist')  # min:-90, max:90
        self.finalMotor = robot.getDevice('finalmotor')
        self.sliderMotor = robot.getDevice('slider')
        self.switcherMotor = robot.getDevice('switchermotor')
        self.nozzleMotor = robot.getDevice('nozzlemotor')
        self.griprotatorMotor = robot.getDevice('griprotatormotor')
        self.topgripperMotor = robot.getDevice('topgrippermotor')
        self.bottomgripperMotor = robot.getDevice('bottomgrippermotor')
        self.distanceMotor = robot.getDevice('distancemotor')
        # MOTOR SENSORS
        self.baseSensor = robot.getDevice('base_sensor')
        self.upperarmSensor = robot.getDevice('upperarm_sensor')
        self.forearmSensor = robot.getDevice('forearm_sensor')
        self.wristSensor = robot.getDevice('wrist_sensor')
        self.rotationalwristSensor = robot.getDevice('rotational_wrist_sensor')
        self.finalSensor = robot.getDevice('finalsensor')
        self.sliderSensor = robot.getDevice('slidersensor')
        self.switcherSensor = robot.getDevice('switchersensor')
        self.nozzleSensor = robot.getDevice('nozzlesensor')
        self.griprotatorSensor = robot.getDevice('griprotatorsensor')
        self.bottomgripperSensor = robot.getDevice('bottomgrippersensor')
        self.topgripperSensor = robot.getDevice('topgrippersensor')
        # ENVIRONMENTAL SENSORS
        #self.distanceSensor = robot.getDevice('ds1')
        self.bottomtouchSensor1 = robot.getDevice('bottomtouchy1')
        self.bottomtouchSensor2 = robot.getDevice('bottomtouchy2')
        self.toptouchSensor1 = robot.getDevice('toptouchy1')
        self.toptouchSensor2 = robot.getDevice('toptouchy2')
        self.cam = robot.getDevice('camera')
        # GLOBAL VARS
        self.prev_time = time.time()
        self.q = np.array([0.0, 0.0, 0.0])
        self.error = np.array([0.0, 0.0, 0.0])
        self.pos_ee = np.array([0.0, 0.0, 0.0])
        self.target = None
        self.TIME_STEP = 32
    
    #method that takes a picture through the camera and
    #saves the picture to the directory containing this file
    def takePicture(self):
        if robot.step(self.TIME_STEP) != -1:
            self.cam.enable(1)
            image = self.cam.getImage()
            image = np.frombuffer(image, np.uint8).reshape((self.cam.getHeight(), self.cam.getWidth(), 4))
            self.cam.saveImage("pic.PNG", 100)
    
    #method that gets the coordinates of the fuel cap from the
    #picture taken by the camera
    def getFuelCapCoords(self):
        coords_centre = np.array([500, -500])
        bounds = detect_fuel_cap("pic.png")
        x_wrtimage = (bounds[1] + bounds[3]) / 2
        y_wrtimage = (bounds[0] + bounds[2]) / 2
        coords_wrtimage = np.array([x_wrtimage, y_wrtimage * -1])
        coords_wrtcentre = coords_wrtimage - coords_centre
        CONV_CONST = 0.000312
        z_distance = coords_wrtcentre[0] * CONV_CONST
        y_distance = coords_wrtcentre[1] * CONV_CONST
        y_distance = 0.07 + y_distance
        x_distance = distanceSensor.getValue()
        return np.array([x_distance, y_distance, z_distance])
    
    #method that calculates the offset by which the
    #proportional-derivative part of the control code moves
    #joint 4 (R4) towards to achieve the required offset position
    def getFuelCapOffset(self):
        x_offset = self.target[0] - 0.19 - 0.05
        y_offset = self.target[1] + 0.116
        z = self.target[2]
        # GET ANGLED TARGS#
        hypo = (0.19 / math.sin(math.radians(75))) * math.sin(math.radians(30))
        x_angledoffset = hypo * math.sin(math.radians(15))
        y_angledoffset = hypo * math.cos(math.radians(15))
        x_offset += x_angledoffset
        y_offset += y_angledoffset
        return np.array([x_offset, y_offset, z])
    
    #method that moves the position of a motor until the
    #desired position is achieved
    def setPositionSync(self, motor, sensor, target, delay):
        DELTA = 0.001
        motor.setPosition(target)
        sensor.enable(self.TIME_STEP)
        effective = None
        while True:
            if robot.step(self.TIME_STEP) == -1:
                break
            delay -= self.TIME_STEP
            effective = sensor.getValue()
            if abs(target - effective) <= DELTA or delay <= 0:
                break
        sensor.disable()
    
    #method that moves the gripper towards the fuel cap until
    #a touch sensor detects a collision
    def moveUntilCollide1(self, motor, sensor, touch1, touch2, target, delay):
        DELTA = 0.001
        motor.setVelocity(target)
        sensor.enable(self.TIME_STEP)
        effective = None
        while True:
            if robot.step(self.TIME_STEP) == -1:
                break
            delay -= self.TIME_STEP
            effective = sensor.getValue()
            touched1 = touch1.getValue()
            touched2 = touch2.getValue()
            if delay <= 0 or touched1 == 1.0 or touched2 == 1.0:
                for i in range(1):
                    if robot.step(self.TIME_STEP) == -1:
                        break
                motor.setPosition(effective)
                break
        sensor.disable()
    
    #method that moves the gripper fingers into the fuel cap
    #bar until a collision is detected
    def moveUntilCollide2(self, motor1, motor2, sensor1, sensor2, touch1, touch2, target, delay):
        motor1.setVelocity(target)
        motor2.setVelocity(target)
        sensor1.enable(self.TIME_STEP)
        sensor2.enable(self.TIME_STEP)
        while True:
            if robot.step(self.TIME_STEP) == -1:
                break
            effective1 = sensor1.getValue()
            effective2 = sensor2.getValue()
            touched1 = touch1.getValue()
            touched2 = touch2.getValue()
            if touched1 == 1.0 and touched2 == 1.0:
                for i in range(1):
                    if robot.step(self.TIME_STEP) == -1:
                        break
                motor1.setPosition(effective1)
                motor2.setPosition(effective2)
                break
    
    #calculates the jacobian of the first 3 joints of the robot
    def jacobian(self):
        fk = sym.Matrix(FKWB())
        thetaB, theta1, theta2 = sym.symbols('thetaB, theta1, theta2')
        joints = sym.Array([thetaB, theta1, theta2])
        jaco = fk.jacobian(joints)
        jaco = jaco.subs(thetaB, self.q[0])
        jaco = jaco.subs(theta1, self.q[1])
        jaco = jaco.subs(theta2, self.q[2])
        jaco = np.array(jaco, dtype='float64')
        return jaco
    
    #find the position of joint 4 (R4) through knowledge
    #of angles of the first 3 joints
    def get_ee(self):
        fk = sym.Array(FKWB())
        thetaB, theta1, theta2 = sym.symbols('thetaB, theta1, theta2')
        fk = fk.subs(thetaB, self.q[0])
        fk = fk.subs(theta1, self.q[1])
        fk = fk.subs(theta2, self.q[2])
        fk = np.array(fk, dtype='float64')
        return fk
    
    #find the angles of the first 3 joints that move joint 4
    #(R4) closer to the target offset position
    def get_new_angles(self):
        pg = 0.6
        dg = 0.5
        K_p = np.array([[pg, 0, 0], [0, pg, 0], [0, 0, pg]])
        K_d = np.array([[dg, 0, 0], [0, dg, 0], [0, 0, dg]])
        current_time = time.time()
        dt = current_time - self.prev_time
        self.prev_time = current_time
        self.pos_ee = self.get_ee()
        error_d = ((self.target - self.pos_ee) - self.error) / dt
        self.error = self.target - self.pos_ee
        J_inv = np.linalg.pinv(self.jacobian())
        dq_d = np.dot(J_inv, (np.dot(K_d, error_d.transpose()) + np.dot(K_p, self.error.transpose())))
        q_d = self.q + (dt * dq_d)
        return q_d
    
    #move the robot towards the fuel cap, unscrew the cap,
    #insert the nozzle into the fuel hole, remove the nozzle,
    #screw the fuel cap back in, and reset the robot
    def activate_robot(self):
        self.takePicture()
        self.prev_time = time.time()
        self.target = self.getFuelCapCoords()
        distanceSensor.disable()
        print(self.target)
        self.target = self.getFuelCapOffset()
        while robot.step(self.TIME_STEP) != -1 and np.linalg.norm(self.target - self.pos_ee) > 0.001:
            joints = self.get_new_angles()
            self.baseMotor.setPosition(joints[0])
            self.upperarmMotor.setPosition(joints[1])
            self.forearmMotor.setPosition(joints[2])
            forth = (joints[1] + joints[2] + 1.5708) * -1
            self.wristMotor.setPosition(forth)
            self.rotationalwristMotor.setPosition(joints[0])
            self.finalMotor.setPosition(-1 * math.radians(30))
            self.q = joints

        self.target[0] += 0.05
        while robot.step(self.TIME_STEP) != -1 and np.linalg.norm(self.target - self.pos_ee) > 0.001:
            joints = self.get_new_angles()
            self.baseMotor.setPosition(joints[0])
            self.upperarmMotor.setPosition(joints[1])
            self.forearmMotor.setPosition(joints[2])
            forth = (joints[1] + joints[2] + 1.5708) * -1
            self.wristMotor.setPosition(forth)
            self.rotationalwristMotor.setPosition(joints[0])
            self.q = joints

        print("In position")

        self.bottomtouchSensor1.enable(1)
        self.toptouchSensor1.enable(1)
        self.sliderMotor.setPosition(float('+inf'))
        self.moveUntilCollide1(self.sliderMotor, self.sliderSensor, self.bottomtouchSensor1, self.toptouchSensor1, 0.01, 10000)
        self.bottomtouchSensor1.disable()
        self.toptouchSensor1.disable()

        print("Contacted fuel cap")

        self.bottomtouchSensor2.enable(1)
        self.toptouchSensor2.enable(1)
        self.topgripperMotor.setPosition(float('+inf'))
        self.bottomgripperMotor.setPosition(float('+inf'))
        self.moveUntilCollide2(self.topgripperMotor, self.bottomgripperMotor, self.topgripperSensor, self.bottomgripperSensor, self.toptouchSensor2,
                          self.bottomtouchSensor2, 0.005, 10000)
        self.bottomtouchSensor2.disable()
        self.toptouchSensor2.disable()

        print("Grasped fuel cap")

        self.setPositionSync(self.griprotatorMotor, self.griprotatorSensor, math.pi, 10000)
        self.setPositionSync(self.sliderMotor, self.sliderSensor, -0.01, 10000)
        self.setPositionSync(self.switcherMotor, self.switcherSensor, math.pi, 10000)
        self.setPositionSync(self.nozzleMotor, self.nozzleSensor, -0.06, 10000)

        print("Inserted nozzle")

        for i in range(30):
            if robot.step(self.TIME_STEP) == -1:
                break
        self.setPositionSync(self.nozzleMotor, self.nozzleSensor, 0.0, 10000)
        self.setPositionSync(self.switcherMotor, self.switcherSensor, math.pi * 2, 10000)
        self.setPositionSync(self.sliderMotor, self.sliderSensor, 0.01, 10000)
        self.setPositionSync(self.griprotatorMotor, self.griprotatorSensor, 0.0, 10000)
        self.setPositionSync(self.topgripperMotor, self.topgripperSensor, 0.0, 10000)
        self.setPositionSync(self.bottomgripperMotor, self.bottomgripperSensor, 0.0, 10000)
        self.setPositionSync(self.sliderMotor, self.sliderSensor, -0.01, 10000)
        self.setPositionSync(self.upperarmMotor, self.upperarmSensor, 0.0, 10000)
        self.setPositionSync(self.finalMotor, self.finalSensor, 0.0, 10000)
        self.setPositionSync(self.rotationalwristMotor, self.rotationalwristSensor, 0.0, 10000)
        self.setPositionSync(self.wristMotor, self.wristSensor, 0.0, 10000)
        self.setPositionSync(self.baseMotor, self.baseSensor, 0.0, 10000)
        self.setPositionSync(self.forearmMotor, self.forearmSensor, 0.0, 10000)

        print("Robot reset")
    
    #method that sets up the TCP connection with the app,
    #and waits for a signal to begin fueling
    def setup_connection(self):
        serverName = 'localhost'
        serverPort = 5050
        clientSocket = socket(AF_INET, SOCK_STREAM)
        clientSocket.connect((serverName, serverPort))
        finished = False
        while robot.step(self.TIME_STEP) != -1 and finished == False:
            message = clientSocket.recv(1024)
            self.activate_robot()
            newMessage = "success#"
            clientSocket.send(newMessage.encode())
            finished = True
    
        clientSocket.close()

robopump = RoboPump()
robopump.setup_connection()
>>>>>>> 9c593a7e1e9e205e2acb7b44c78db8ad92ee9a62
