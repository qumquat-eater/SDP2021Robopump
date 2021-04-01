<<<<<<< HEAD
"""joint_rotator_test controller."""

# You may need to import some classes of the controller module. Ex:
#  from controller import Robot, Motor, DistanceSensor
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
TIME_STEP = 32

# create the Robot instance.
robot = Robot()
#MOTORS
baseMotor = robot.getDevice('base') #min:-90, max:90
upperarmMotor = robot.getDevice('upperarm') #min:-90, max:90
forearmMotor = robot.getDevice('forearm') #min:-90, max:90
wristMotor = robot.getDevice('wrist') #min:-90, max:90
rotationalwristMotor = robot.getDevice('rotational_wrist') #min:-90, max:90
finalMotor = robot.getDevice('finalmotor')
sliderMotor = robot.getDevice('slider')
switcherMotor = robot.getDevice('switchermotor')
nozzleMotor = robot.getDevice('nozzlemotor')
griprotatorMotor = robot.getDevice('griprotatormotor')
topgripperMotor = robot.getDevice('topgrippermotor')
bottomgripperMotor = robot.getDevice('bottomgrippermotor')
distanceMotor = robot.getDevice('distancemotor')
#MOTOR SENSORS
baseSensor = robot.getDevice('base_sensor')
forearmSensor = robot.getDevice('forearm_sensor')
wristSensor = robot.getDevice('wrist_sensor')
rotationalwristSensor = robot.getDevice('rotational_wrist_sensor')
sliderSensor = robot.getDevice('slidersensor')
switcherSensor = robot.getDevice('switchersensor')
nozzleSensor = robot.getDevice('nozzlesensor')
griprotatorSensor = robot.getDevice('griprotatorsensor')
bottomgripperSensor = robot.getDevice('bottomgrippersensor')
topgripperSensor = robot.getDevice('topgrippersensor')
#SENSORS
distanceSensor = robot.getDevice('ds1')
distanceSensor.enable(1)
bottomtouchSensor1 = robot.getDevice('bottomtouchy1')
bottomtouchSensor2 = robot.getDevice('bottomtouchy2')
toptouchSensor1 = robot.getDevice('toptouchy1')
toptouchSensor2 = robot.getDevice('toptouchy2')

cam = robot.getDevice('camera')
cam.enable(1)

if robot.step(TIME_STEP) != -1:
    image = cam.getImage()
    image = np.frombuffer(image, np.uint8).reshape((cam.getHeight(), cam.getWidth(), 4))
    cam.saveImage("pic.PNG", 100)
    
#cv2.imshow("preview", image)
#cv2.waitKey(2000)
# 0.0002604131346114286
#CONV_CONST_X = 0.0002604131346114286
#CONV_CONST_Y = 0.00028
coords_centre = np.array([500, -500])

bounds = detect_fuel_cap("pic.png")
x_wrtimage = (bounds[1] + bounds[3]) / 2
y_wrtimage = (bounds[0] + bounds[2]) / 2
coords_wrtimage = np.array([x_wrtimage, y_wrtimage*-1])
coords_wrtcentre = coords_wrtimage - coords_centre
print("WRT COORDS " + str(coords_wrtcentre))

CONV_CONST_X = 0.00017
CONV_CONST_Y = 0.00022
CONV_CONST = 0.000312

z_distance = coords_wrtcentre[0] * CONV_CONST

y_distance = coords_wrtcentre[1] * CONV_CONST
y_distance = 0.07 + y_distance

x_distance = distanceSensor.getValue()
print(x_distance)
distanceSensor.disable()

def setPositionSync(motor, sensor, target, delay):
    DELTA = 0.001
    motor.setPosition(target)
    sensor.enable(TIME_STEP)
    effective = None
    while True:
        if robot.step(TIME_STEP) == -1:
            break
        delay -= TIME_STEP
        effective = sensor.getValue()
        if abs(target - effective) <= DELTA or delay <= 0:
            break
    sensor.disable()

def moveUntilCollide1(motor, sensor, touch1, touch2, target, delay):
    DELTA = 0.001
    motor.setVelocity(target)
    sensor.enable(TIME_STEP)
    effective = None
    while True:
        if robot.step(TIME_STEP) == -1:
            break
        delay -= TIME_STEP
        effective = sensor.getValue()
        touched1 = touch1.getValue()
        touched2 = touch2.getValue()
        if delay <= 0 or touched1 == 1.0 or touched2 == 1.0:
            print(1 + touched1)
            print(2 + touched2)
            for i in range(1):
                if robot.step(TIME_STEP) == -1:
                    break
            motor.setPosition(effective)
            break
    sensor.disable()

def moveUntilCollide2(motor1, motor2, sensor1, sensor2, touch1, touch2, target, delay):
    motor1.setVelocity(target)
    motor2.setVelocity(target)
    sensor1.enable(TIME_STEP)
    sensor2.enable(TIME_STEP)
    while True:
        if robot.step(TIME_STEP) == -1:
            break
        effective1 = sensor1.getValue()
        effective2 = sensor2.getValue()
        touched1 = touch1.getValue()
        touched2 = touch2.getValue()
        if touched1 == 1.0 and touched2 == 1.0:
            for i in range(1):
                if robot.step(TIME_STEP) == -1:
                    break
            motor1.setPosition(effective1)
            motor2.setPosition(effective2)
            break

def jacobian(qs):
    fk = sym.Matrix(FKWB())
    thetaB, theta1, theta2 = sym.symbols('thetaB, theta1, theta2')
    joints = sym.Array([thetaB, theta1, theta2])
    jaco = fk.jacobian(joints)
    jaco = jaco.subs(thetaB, qs[0])
    jaco = jaco.subs(theta1, qs[1])
    jaco = jaco.subs(theta2, qs[2])
    jaco = np.array(jaco, dtype='float64')
    return jaco

def get_ee(qs):
    fk = sym.Array(FKWB())
    thetaB, theta1, theta2 = sym.symbols('thetaB, theta1, theta2')
    fk = fk.subs(thetaB, qs[0])
    fk = fk.subs(theta1, qs[1])
    fk = fk.subs(theta2, qs[2])
    fk = np.array(fk, dtype='float64')
    return fk

def get_new_angles(qs):
    pg = 0.6
    dg = 0.5
    K_p = np.array([[pg, 0, 0], [0, pg, 0], [0, 0, pg]])
    K_d = np.array([[dg, 0, 0], [0, dg, 0], [0, 0, dg]])
    current_time = time.time()
    global prev_time
    global error
    global final_joints
    global pos_ee, pos_targ, pos_base_abs
    global x_biased, y_biased, z
    dt = current_time - prev_time
    prev_time = current_time
    pos_ee = get_ee(qs)
    #2nd last link = 0.1485
    #last link = 0.13
    #distance from tank = 0.05
    pos_base = pos_base_abs
    pos_targ = np.array([x_biased, y_biased, z])
    #pos_targ = pos_targ - pos_base
    error_d = ((pos_targ - pos_ee) - error) / dt
    error = pos_targ - pos_ee
    J_inv = np.linalg.pinv(jacobian(qs))
    dq_d = np.dot(J_inv, (np.dot(K_d, error_d.transpose()) + np.dot(K_p, error.transpose())))
    q_d = qs + (dt * dq_d)
    return q_d

final_joints = None
previous_time = time.time()
error = np.array([0.0, 0.0, 0.0])
pos_base_abs = np.array([0.0765488, 0.2035, -0.208071])

###EDIT THIS TO THE POSITION OF TARGET###################
###INPUT THE "TRANSLATION" VALUES OF TARGET INTO TARG1###
'''
physical_target = np.array([0.3521, 0.2053+0.001, -0.18])
physical_target = physical_target - pos_base_abs

x_biased = physical_target[0] - 0.18
y_biased = physical_target[1] + 0.116
z = physical_target[2]
'''
targ1 = np.array([x_distance, y_distance, z_distance])
targ_now = targ1

x_biased = targ_now[0] - 0.19
y_biased = targ_now[1] + 0.116
z = targ_now[2]



#GET ANGLED TARGS#
hypo = (0.19/math.sin(math.radians(75))) * math.sin(math.radians(30))
x_offset = hypo * math.sin(math.radians(15))
y_offset = hypo * math.cos(math.radians(15))

x_biased += x_offset
y_biased += y_offset

#STAGE1#

q = np.array([0.0, 0.0, 0.0])
pos_ee = np.array([0.0, 0.0, 0.0])
pos_targ = np.array([10.0, 10.0, 10.0])
prev_time = time.time()

def activate_robot():
    global q
    #prev_time = time.time()
    while robot.step(TIME_STEP) != -1 and np.linalg.norm(pos_targ - pos_ee) > 0.001:
        joints = get_new_angles(q)
        baseMotor.setPosition(joints[0])
        upperarmMotor.setPosition(joints[1])
        forearmMotor.setPosition(joints[2])
        forth = (joints[1] + joints[2] + 1.5708) * -1
        wristMotor.setPosition(forth)
        rotationalwristMotor.setPosition(joints[0])
        finalMotor.setPosition(-1*math.radians(30))
        q = joints
        final_joints = joints
    '''
    while robot.step(TIME_STEP) != -1 and np.linalg.norm(pos_targ - pos_ee) > 0.001:
        joints = get_new_angles(q)
        baseMotor.setPosition(joints[0])
        upperarmMotor.setPosition(joints[1])
        forearmMotor.setPosition(joints[2])
        forth = (joints[1] + joints[2] + 1.5708) * -1
        wristMotor.setPosition(forth)
        q = joints
        final_joints = joints
    '''
    
    print("In position")
    
    #STAGE2#
    bottomtouchSensor1.enable(1)
    toptouchSensor1.enable(1)
    sliderMotor.setPosition(float('+inf'))
    moveUntilCollide1(sliderMotor, sliderSensor, bottomtouchSensor1, toptouchSensor1, 0.01, 10000)
    bottomtouchSensor1.disable()
    toptouchSensor1.disable()
    
    print("Contacted fuel cap")
    
    bottomtouchSensor2.enable(1)
    toptouchSensor2.enable(1)
    topgripperMotor.setPosition(float('+inf'))
    bottomgripperMotor.setPosition(float('+inf'))
    moveUntilCollide2(topgripperMotor, bottomgripperMotor, topgripperSensor, bottomgripperSensor, toptouchSensor2, bottomtouchSensor2, 0.005, 10000)
    bottomtouchSensor2.disable()
    toptouchSensor2.disable()
    
    print("Grasped fuel cap")
    
    setPositionSync(griprotatorMotor, griprotatorSensor, math.pi, 10000)
    setPositionSync(sliderMotor, sliderSensor, -0.01, 10000)
    setPositionSync(switcherMotor, switcherSensor, math.pi, 10000)
    setPositionSync(nozzleMotor, nozzleSensor, -0.06, 10000)
    
    print("Inserted nozzle")
    
    for i in range(30):
        if robot.step(TIME_STEP) == -1:
            break
    setPositionSync(nozzleMotor, nozzleSensor, 0.0, 10000)
    setPositionSync(switcherMotor, switcherSensor, math.pi*2, 10000)
    setPositionSync(sliderMotor, sliderSensor, 0.01, 10000)
    setPositionSync(griprotatorMotor, griprotatorSensor, 0.0, 10000)
    setPositionSync(topgripperMotor, topgripperSensor, 0.0, 10000)
    setPositionSync(bottomgripperMotor, bottomgripperSensor, 0.0, 10000)
    setPositionSync(sliderMotor, sliderSensor, -0.01, 10000)
    finalMotor.setPosition(0.0)
    rotationalwristMotor.setPosition(0.0)
    wristMotor.setPosition(0.0)
    baseMotor.setPosition(0.0)
    upperarmMotor.setPosition(0.0)
    forearmMotor.setPosition(0.0)
    
    print("Robot reset")

serverName = 'localhost'
serverPort = 5050

def setup_connection():
    clientSocket = socket(AF_INET, SOCK_STREAM)
    clientSocket.connect((serverName, serverPort))
    finished = False
    while robot.step(TIME_STEP) != -1 and finished == False:
        message = clientSocket.recv(1024)
        print(message.decode())
        #if message.decode() == "Full#":
        global prev_time
        prev_time = time.time()
        activate_robot()
        newMessage = "success#"
        clientSocket.send(newMessage.encode())
        finished = True
        #else:
            #print("Oh")

    clientSocket.close()

activate_robot()


=======
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

    def takePicture(self):
        if robot.step(self.TIME_STEP) != -1:
            self.cam.enable(1)
            image = self.cam.getImage()
            image = np.frombuffer(image, np.uint8).reshape((self.cam.getHeight(), self.cam.getWidth(), 4))
            self.cam.saveImage("pic.PNG", 100)

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

    def get_ee(self):
        fk = sym.Array(FKWB())
        thetaB, theta1, theta2 = sym.symbols('thetaB, theta1, theta2')
        fk = fk.subs(thetaB, self.q[0])
        fk = fk.subs(theta1, self.q[1])
        fk = fk.subs(theta2, self.q[2])
        fk = np.array(fk, dtype='float64')
        return fk

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

        # STAGE2#
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
