import sympy as sym

def transMatrix():
    theta, d, a, alpha = sym.symbols('theta, d, a, alpha')
    r1 = sym.Array([sym.cos(theta), -sym.sin(theta) * sym.cos(alpha), sym.sin(theta) * sym.sin(alpha), a * sym.cos(theta)])
    r2 = sym.Array([sym.sin(theta), sym.cos(theta) * sym.cos(alpha), -sym.cos(theta) * sym.sin(alpha), a * sym.sin(theta)])
    r3 = sym.Array([0, sym.sin(alpha), sym.cos(alpha), d])
    r4 = sym.Array([0, 0, 0, 1])
    return sym.Matrix([r1, r2, r3, r4])

def TM01():
    matrix = transMatrix()
    theta, d, a, alpha = sym.symbols('theta, d, a, alpha')
    matrix = matrix.subs(theta, sym.pi/2)
    matrix = matrix.subs(d, 0)
    matrix = matrix.subs(a, 0)
    matrix = matrix.subs(alpha, 0)
    return matrix
    
def WB():
    matrix = transMatrix()
    thetaB = sym.symbols('thetaB')
    theta, d, a, alpha = sym.symbols('theta, d, a, alpha')
    matrix = matrix.subs(theta, 0)
    matrix = matrix.subs(d, 0)
    matrix = matrix.subs(a, 0)
    matrix = matrix.subs(alpha, thetaB)
    return matrix

def TM12():
    matrix = transMatrix()
    theta1 = sym.symbols('theta1')
    theta, d, a, alpha = sym.symbols('theta, d, a, alpha')
    matrix = matrix.subs(theta, theta1)
    matrix = matrix.subs(d, 0)
    matrix = matrix.subs(a, 0.189999)
    matrix = matrix.subs(alpha, 0)
    return matrix

def TM23():
    matrix = transMatrix()
    theta2 = sym.symbols('theta2')
    theta, d, a, alpha = sym.symbols('theta, d, a, alpha')
    matrix = matrix.subs(theta, theta2)
    matrix = matrix.subs(d, 0)
    matrix = matrix.subs(a, 0.139)
    matrix = matrix.subs(alpha, 0)
    return matrix

def WB1():
    return TM01() * WB()

def WB02():
    return WB1() * TM12()

def WB03():
    return WB02() * TM23()

def FKWB():
    final_col = WB03().col(-1)
    x = final_col.row(0)
    y = final_col.row(1)
    z = final_col.row(2)
    return sym.Array([x, y, z])