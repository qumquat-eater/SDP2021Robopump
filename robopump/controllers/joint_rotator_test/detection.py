import detect_shapes as ds
import custom_model_images as cm
import Eclipse_detection as ed
import cv2
import argparse
from PIL import Image
import numpy as np


def detect_fuel_cap(image_path):

    try:
        image_np, img_coords = cm.run(image_path)
        img = Image.fromarray(image_np)
        crop_img =  np.array(img.crop((img_coords[1], img_coords[0], img_coords[3], img_coords[2])))
        #crop_img = image_np[img_coords[0]:img_coords[2], img_coords[1]:img_coords[3]]
        cv2.imwrite("output/cropped.png", crop_img)
    except:
        print('Hatch has not been found')
        return

    try:
        pass
        #vertices, centroid, width, rotate_angle = ds.find_shape(crop_img)
    except Exception as e:
        print(e)
        return


    try:
        pass
        #slant, phi = ed.get_cap_info(crop_img)
    except:
        print('Slant and Phi can not be calculated')
        return

    print('Fuel cap found')
    file = open("fuel_cap_coordinates.txt", "w")
    '''
    file.write("%s\n" % slant)
    file.write("%s\n" % phi)
    file.write("%s\n" % vertices)
    file.write("{} {}\n".format(centroid[0],centroid[1]))
    file.write("%s\n" % width)
    file.write("%s\n" % rotate_angle)
    '''

    return img_coords

'''
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,
    help="path to the input image")
args = vars(ap.parse_args())

detect_fuel_cap(args["image"])
'''
